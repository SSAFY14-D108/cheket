import { http, HttpResponse } from "msw"
import {
  getNextShowId,
  getShowDetailSnapshot,
  getVenueAddress,
  mockEventStore,
  mockSectionsByVenue,
  mockTicketEffects,
  mockVenues,
  myPageShowsStore,
} from "@/mocks/data/show-store"
import { createNotFoundResponse, createUnauthorizedResponse, isAuthorized } from "./utils"

export const showHandlers = [
  http.get("*/api/v1/hosts/shows/effect", async ({ request }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "티켓 효과 목록 조회 완료",
        data: mockTicketEffects,
      },
      { status: 200 }
    )
  }),
  http.get("*/api/v1/hosts/shows/:venueId/sections", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    const venueId = Number(params.venueId)

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "구역 목록 조회 완료",
        data: mockSectionsByVenue[venueId] ?? [],
      },
      { status: 200 }
    )
  }),
  http.get("*/api/v1/hosts/shows/:showId", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    const snapshot = getShowDetailSnapshot(params.showId)

    if (!snapshot) {
      return createNotFoundResponse()
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "공연 상세 조회 성공",
        data: snapshot,
      },
      { status: 200 }
    )
  }),
  http.post("*/api/v1/hosts/shows", async ({ request }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    const body = (await request.json()) as {
      title?: string
      posterUrl?: string
      venueId?: number
      artistName?: string
      show?: { startAt?: string; endAt?: string }
      reservation?: { openAt?: string; closeAt?: string }
      description?: string
      purchaseLimit?: number
      grade?: Array<{
        sectionId?: number[]
        gradeName?: string
        price?: number
        colorCode?: string
        ticketEffectId?: number
      }>
      stakeholders?: Array<{
        role?: "organizer" | "artist"
        shareBps?: number
      }>
      refundPolicy?: Array<{
        daysRemaining?: number
        refundRate?: number
      }>
      sessionInfo?: Array<{
        sessionId?: number
        sessionDate?: string
        sessionStartDate?: string
      }>
    }

    if (!body.title || !body.venueId) {
      return HttpResponse.json(
        {
          httpStatusCode: 400,
          errorMessage: "필수 입력값이 누락되었습니다.",
        },
        { status: 400 }
      )
    }

    const nextShowId = getNextShowId()
    const selectedVenue = mockVenues.find((venue) => venue.venueId === body.venueId)
    const venueName = selectedVenue?.name ?? `공연장 ${body.venueId}`
    const newShowListItem = {
      showId: nextShowId,
      title: body.title,
      posterUrl: body.posterUrl ?? "/images/poster-1.jpg",
      venue: venueName,
      purchaseLimit: body.purchaseLimit ?? 1,
      region: "SEOUL",
      show: {
        showStartDate: body.show?.startAt?.slice(0, 10) ?? "2026-01-01",
        showEndDate:
          body.show?.endAt?.slice(0, 10) ?? body.show?.startAt?.slice(0, 10) ?? "2026-01-01",
      },
      reservation: {
        startDate: body.reservation?.openAt ?? "2026-01-01T10:00:00",
        endDate: body.reservation?.closeAt ?? "2026-01-02T10:00:00",
      },
      status: "DRAFT",
    }
    const newEvent = {
      showId: nextShowId,
      title: body.title,
      artistName: body.artistName ?? "미정",
      posterUrl: body.posterUrl ?? "/images/poster-1.jpg",
      venue: {
        venueId: body.venueId,
        name: venueName,
        address: getVenueAddress(body.venueId),
      },
      show: {
        startAt: body.show?.startAt ?? "2026-01-01T19:00:00",
        endAt: body.show?.endAt ?? "2026-01-01T21:00:00",
      },
      reservation: {
        openAt: body.reservation?.openAt ?? "2026-01-01T10:00:00",
        closeAt: body.reservation?.closeAt ?? "2026-01-02T10:00:00",
      },
      description: body.description ?? "",
      purchaseLimit: body.purchaseLimit ?? 1,
      grade:
        body.grade?.map((grade) => ({
          sectionId: grade.sectionId?.[0] ?? 1,
          gradeName: grade.gradeName ?? "일반",
          price: grade.price ?? 0,
          colorCode: grade.colorCode ?? "#7C6EF0",
          ...(grade.ticketEffectId ? { ticketEffectId: grade.ticketEffectId } : {}),
        })) ?? [],
      stakeholders:
        body.stakeholders?.map((stakeholder, index) => ({
          role: stakeholder.role ?? "artist",
          name: stakeholder.role === "organizer" ? `주최측 ${index + 1}` : `아티스트 ${index + 1}`,
          shareBps: stakeholder.shareBps ?? 0,
        })) ?? [],
      refundPolicy:
        body.refundPolicy?.map((policy) => ({
          daysRemaining: policy.daysRemaining ?? 0,
          refundRate: policy.refundRate ?? 0,
        })) ?? [],
      sessionInfo:
        body.sessionInfo?.map((session, index) => ({
          sessionId: session.sessionId ?? index + 1,
          sessionDate: session.sessionDate ?? "2026-01-01",
          sessionStartDate: session.sessionStartDate ?? "19:00",
          capacity: selectedVenue?.capacity ?? 1000,
        })) ?? [],
      status: "DRAFT",
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      capacity: selectedVenue?.capacity ?? 1000,
      soldSeats: 0,
      enteredCount: 0,
      notEnteredCount: 0,
      emptyCount: selectedVenue?.capacity ?? 1000,
      likes: 0,
    }

    myPageShowsStore.push(newShowListItem)
    mockEventStore.push(newEvent)

    return HttpResponse.json(
      {
        httpStatusCode: 201,
        responseMessage: "공연 등록 완료",
        data: {
          showId: nextShowId,
        },
      },
      { status: 201 }
    )
  }),
  http.put("*/api/v1/hosts/shows/:showId", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    const snapshot = getShowDetailSnapshot(params.showId)

    if (!snapshot) {
      return createNotFoundResponse()
    }

    const body = (await request.json()) as {
      title?: string
      posterUrl?: string
      artistName?: string
      venueId?: number
      show?: { startAt?: string; endAt?: string }
      reservation?: { openAt?: string; closeAt?: string }
      description?: string
      purchaseLimit?: number
      grade?: Array<{
        sectionId?: number[]
        gradeName?: string
        price?: number
        colorCode?: string
        ticketEffectId?: number
      }>
      stakeholders?: Array<{
        role?: "organizer" | "artist"
        shareBps?: number
      }>
      refundPolicy?: Array<{
        daysRemaining?: number
        refundRate?: number
      }>
      sessionInfo?: Array<{
        sessionId?: number
        sessionDate?: string
        sessionStartDate?: string
      }>
    }

    if (
      !body.title &&
      !body.posterUrl &&
      !body.artistName &&
      !body.venueId &&
      !body.show?.startAt &&
      !body.show?.endAt &&
      !body.reservation?.openAt &&
      !body.reservation?.closeAt &&
      !body.description &&
      body.purchaseLimit === undefined &&
      !body.grade &&
      !body.stakeholders &&
      !body.refundPolicy &&
      !body.sessionInfo
    ) {
      return HttpResponse.json(
        {
          httpStatusCode: 400,
          errorMessage: "수정할 정보가 없습니다.",
        },
        { status: 400 }
      )
    }

    const showId = Number(params.showId)
    const eventIndex = mockEventStore.findIndex((event) => event.showId === showId)
    const myPageShowIndex = myPageShowsStore.findIndex((show) => show.showId === showId)

    if (eventIndex >= 0) {
      const previousEvent = mockEventStore[eventIndex]
      const nextVenueId = body.venueId ?? previousEvent.venue.venueId
      const selectedVenue = mockVenues.find((venue) => venue.venueId === nextVenueId)

      mockEventStore[eventIndex] = {
        ...previousEvent,
        title: body.title ?? previousEvent.title,
        artistName: body.artistName ?? previousEvent.artistName,
        posterUrl: body.posterUrl ?? previousEvent.posterUrl,
        venue: {
          venueId: nextVenueId,
          name: selectedVenue?.name ?? previousEvent.venue.name,
          address: selectedVenue ? getVenueAddress(nextVenueId) : previousEvent.venue.address,
        },
        show: {
          startAt: body.show?.startAt ?? previousEvent.show.startAt,
          endAt: body.show?.endAt ?? previousEvent.show.endAt,
        },
        reservation: {
          openAt: body.reservation?.openAt ?? previousEvent.reservation.openAt,
          closeAt: body.reservation?.closeAt ?? previousEvent.reservation.closeAt,
        },
        description: body.description ?? previousEvent.description,
        purchaseLimit: body.purchaseLimit ?? previousEvent.purchaseLimit,
        grade:
          body.grade?.map((grade) => ({
            sectionId: grade.sectionId?.[0] ?? previousEvent.grade[0]?.sectionId ?? 1,
            gradeName: grade.gradeName ?? "일반",
            price: grade.price ?? 0,
            colorCode: grade.colorCode ?? "#7C6EF0",
            ...(grade.ticketEffectId ? { ticketEffectId: grade.ticketEffectId } : {}),
          })) ?? previousEvent.grade,
        stakeholders:
          body.stakeholders?.map((stakeholder, index) => ({
            role: stakeholder.role ?? "artist",
            name: stakeholder.role === "organizer" ? `주최측 ${index + 1}` : `아티스트 ${index + 1}`,
            shareBps: stakeholder.shareBps ?? 0,
          })) ?? previousEvent.stakeholders,
        refundPolicy:
          body.refundPolicy?.map((policy) => ({
            daysRemaining: policy.daysRemaining ?? 0,
            refundRate: policy.refundRate ?? 0,
          })) ?? previousEvent.refundPolicy,
        sessionInfo:
          body.sessionInfo?.map((session, index) => ({
            sessionId: session.sessionId ?? index + 1,
            sessionDate: session.sessionDate ?? "2026-01-01",
            sessionStartDate: session.sessionStartDate ?? "19:00",
            capacity: selectedVenue?.capacity ?? previousEvent.capacity,
          })) ?? previousEvent.sessionInfo,
        updatedAt: new Date().toISOString(),
      }
    }

    if (myPageShowIndex >= 0) {
      const previousShow = myPageShowsStore[myPageShowIndex]
      const nextVenueId = body.venueId ?? mockEventStore[eventIndex]?.venue.venueId
      const selectedVenue = mockVenues.find((venue) => venue.venueId === nextVenueId)

      myPageShowsStore[myPageShowIndex] = {
        ...previousShow,
        title: body.title ?? previousShow.title,
        posterUrl: body.posterUrl ?? previousShow.posterUrl,
        venue: selectedVenue?.name ?? previousShow.venue,
        purchaseLimit: body.purchaseLimit ?? previousShow.purchaseLimit,
        show: {
          showStartDate: body.show?.startAt?.slice(0, 10) ?? previousShow.show.showStartDate,
          showEndDate: body.show?.endAt?.slice(0, 10) ?? previousShow.show.showEndDate,
        },
        reservation: {
          startDate: body.reservation?.openAt ?? previousShow.reservation.startDate,
          endDate: body.reservation?.closeAt ?? previousShow.reservation.endDate,
        },
      }
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "수정이 완료되었습니다.",
      },
      { status: 200 }
    )
  }),
  http.delete("*/api/v1/hosts/shows/:showId", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    const snapshot = getShowDetailSnapshot(params.showId)

    if (!snapshot) {
      return createNotFoundResponse()
    }

    const showId = Number(params.showId)
    const myPageShowIndex = myPageShowsStore.findIndex((show) => show.showId === showId)
    const eventIndex = mockEventStore.findIndex((event) => event.showId === showId)

    if (myPageShowIndex >= 0) {
      myPageShowsStore.splice(myPageShowIndex, 1)
    }

    if (eventIndex >= 0) {
      mockEventStore.splice(eventIndex, 1)
    }

    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "공연 정보 삭제가 완료되었습니다.",
      },
      { status: 200 }
    )
  }),
]
