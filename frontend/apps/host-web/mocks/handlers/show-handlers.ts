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

type ShowMutationBody = {
  title?: string
  venueId?: number
  artist?: string
  playtime?: number
  showStartDate?: string
  showEndDate?: string
  reservationStartDate?: string
  reservationEndDate?: string
  description?: string
  purchaseLimit?: number
  grade?: Array<{
    sectionIds?: number[]
    gradeName?: string
    price?: number
    colorCode?: string
    ticketEffectId?: number
  }>
  stakeholders?: Array<{
    role?: "ORGANIZER" | "ARTIST"
    businessNo?: string | null
    phoneNumber?: string | null
    shareBps?: number
  }>
  refundPolicy?: Array<{
    daysRemaining?: number
    refundRate?: number
  }>
  sessionInfo?: Array<{
    sessionDate?: string
    sessionStartTime?: string
  }>
  existingDescriptionImageUrls?: string[] | null
}

async function parseShowMutationBody(request: Request) {
  const formData = await request.formData()
  const showPart = formData.get("show")

  if (typeof showPart === "string") {
    return {
      body: JSON.parse(showPart) as ShowMutationBody,
      formData,
    }
  }

  if (showPart instanceof File) {
    return {
      body: (JSON.parse(await showPart.text()) as ShowMutationBody) ?? {},
      formData,
    }
  }

  return {
    body: {} as ShowMutationBody,
    formData,
  }
}

function toDateOnly(value?: string) {
  return value ? value.slice(0, 10) : "2026-01-01"
}

function toTimeOnly(value?: string) {
  if (!value) {
    return "19:00"
  }

  return value.includes("T") ? value.slice(11, 16) : value.slice(0, 5)
}

function mapStakeholders(body: ShowMutationBody) {
  return (
    body.stakeholders?.map((stakeholder, index): {
      role: "ORGANIZER" | "ARTIST"
      id: number
      name: string
      businessNo?: string
      phone?: string
      shareBps: number
    } => ({
      role: stakeholder.role === "ORGANIZER" ? "ORGANIZER" : "ARTIST",
      id: 100 + index,
      name: stakeholder.role === "ORGANIZER" ? `주최측 ${index + 1}` : `아티스트 ${index + 1}`,
      businessNo: stakeholder.role === "ORGANIZER" ? stakeholder.businessNo ?? undefined : undefined,
      phone: stakeholder.role === "ARTIST" ? stakeholder.phoneNumber ?? undefined : undefined,
      shareBps: stakeholder.shareBps ?? 0,
    })) ?? []
  )
}

function getDescriptionImageUrls(formData: FormData) {
  return formData
    .getAll("descriptionImages")
    .filter((entry): entry is File => entry instanceof File)
    .map(
      (_, index) => `http://localhost:3000/images/poster-${(index % 4) + 1}.jpg`
    )
}

function mapSessions(body: ShowMutationBody, capacity: number) {
  return (
    body.sessionInfo?.map((session, index) => ({
      sessionId: index + 1,
      sessionDate: toDateOnly(session.sessionDate),
      sessionStartDate: toTimeOnly(session.sessionStartTime),
      capacity,
    })) ?? []
  )
}

export const showHandlers = [
  http.get("*/api/v1/shows/venue", async () => {
    return HttpResponse.json(
      {
        httpStatusCode: 200,
        responseMessage: "공연장 목록 조회 완료",
        data: mockVenues,
      },
      { status: 200 }
    )
  }),
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
  http.get("*/api/v1/hosts/venues/:venueId/sections", async ({ request, params }) => {
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

    const { body, formData } = await parseShowMutationBody(request)
    const posterImage = formData.get("posterImage")
    const descriptionImageUrls = getDescriptionImageUrls(formData)

    if (!body.title || !body.venueId || !(posterImage instanceof File)) {
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
    const capacity = selectedVenue?.capacity ?? 1000

    myPageShowsStore.push({
      showId: nextShowId,
      title: body.title,
      posterUrl: "/images/poster-1.jpg",
      venue: venueName,
      purchaseLimit: body.purchaseLimit ?? 1,
      region: "SEOUL",
      show: {
        showStartDate: toDateOnly(body.showStartDate),
        showEndDate: toDateOnly(body.showEndDate ?? body.showStartDate),
      },
      reservation: {
        startDate: body.reservationStartDate ?? "2026-01-01T10:00:00",
        endDate: body.reservationEndDate ?? "2026-01-02T10:00:00",
      },
      status: "DRAFT",
    })

    mockEventStore.push({
      showId: nextShowId,
      title: body.title,
      artistName: body.artist ?? "미정",
      playtime: body.playtime ?? 120,
      posterUrl: "/images/poster-1.jpg",
      venue: {
        venueId: body.venueId,
        name: venueName,
        address: getVenueAddress(body.venueId),
      },
      show: {
        startAt: body.showStartDate ?? "2026-01-01T19:00:00",
        endAt: body.showEndDate ?? "2026-01-01T21:00:00",
      },
      reservation: {
        openAt: body.reservationStartDate ?? "2026-01-01T10:00:00",
        closeAt: body.reservationEndDate ?? "2026-01-02T10:00:00",
      },
      description: body.description ?? "",
      descriptionImages: descriptionImageUrls,
      purchaseLimit: body.purchaseLimit ?? 1,
      grade:
        body.grade?.map((grade) => ({
          sectionId: grade.sectionIds?.[0] ?? 1,
          gradeName: grade.gradeName ?? "일반",
          price: grade.price ?? 0,
          colorCode: grade.colorCode ?? "#7C6EF0",
          ...(grade.ticketEffectId ? { ticketEffectId: grade.ticketEffectId } : {}),
        })) ?? [],
      stakeholders: mapStakeholders(body),
      refundPolicy:
        body.refundPolicy?.map((policy) => ({
          daysRemaining: policy.daysRemaining ?? 0,
          refundRate: policy.refundRate ?? 0,
        })) ?? [],
      sessionInfo: mapSessions(body, capacity),
      status: "DRAFT",
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      capacity,
      soldSeats: 0,
      enteredCount: 0,
      notEnteredCount: 0,
      emptyCount: capacity,
      likes: 0,
    })

    return HttpResponse.json(
      {
        httpStatusCode: 201,
        responseMessage: "공연 등록 완료",
        data: nextShowId,
      },
      { status: 201 }
    )
  }),
  http.patch("*/api/v1/hosts/shows/:showId", async ({ request, params }) => {
    if (!isAuthorized(request)) {
      return createUnauthorizedResponse()
    }

    const snapshot = getShowDetailSnapshot(params.showId)

    if (!snapshot) {
      return createNotFoundResponse()
    }

    const { body, formData } = await parseShowMutationBody(request)

    if (!body.title || !body.venueId) {
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
      const capacity = selectedVenue?.capacity ?? previousEvent.capacity
      const posterImage = formData.get("posterImage")
      const nextDescriptionImageUrls = getDescriptionImageUrls(formData)
      const retainedDescriptionImageUrls = body.existingDescriptionImageUrls

      const descriptionImages =
        retainedDescriptionImageUrls === null || typeof retainedDescriptionImageUrls === "undefined"
          ? previousEvent.descriptionImages ?? []
          : [...retainedDescriptionImageUrls, ...nextDescriptionImageUrls]

      mockEventStore[eventIndex] = {
        ...previousEvent,
        title: body.title ?? previousEvent.title,
        artistName: body.artist ?? previousEvent.artistName,
        playtime: body.playtime ?? previousEvent.playtime,
        posterUrl: posterImage instanceof File ? previousEvent.posterUrl : previousEvent.posterUrl,
        venue: {
          venueId: nextVenueId,
          name: selectedVenue?.name ?? previousEvent.venue.name,
          address: selectedVenue ? getVenueAddress(nextVenueId) : previousEvent.venue.address,
        },
        show: {
          startAt: body.showStartDate ?? previousEvent.show.startAt,
          endAt: body.showEndDate ?? previousEvent.show.endAt,
        },
        reservation: {
          openAt: body.reservationStartDate ?? previousEvent.reservation.openAt,
          closeAt: body.reservationEndDate ?? previousEvent.reservation.closeAt,
        },
        description: body.description ?? previousEvent.description,
        descriptionImages,
        purchaseLimit: body.purchaseLimit ?? previousEvent.purchaseLimit,
        grade:
          body.grade?.map((grade) => ({
            sectionId: grade.sectionIds?.[0] ?? previousEvent.grade[0]?.sectionId ?? 1,
            gradeName: grade.gradeName ?? "일반",
            price: grade.price ?? 0,
            colorCode: grade.colorCode ?? "#7C6EF0",
            ...(grade.ticketEffectId ? { ticketEffectId: grade.ticketEffectId } : {}),
          })) ?? previousEvent.grade,
        stakeholders: body.stakeholders ? mapStakeholders(body) : previousEvent.stakeholders,
        refundPolicy:
          body.refundPolicy?.map((policy) => ({
            daysRemaining: policy.daysRemaining ?? 0,
            refundRate: policy.refundRate ?? 0,
          })) ?? previousEvent.refundPolicy,
        sessionInfo: body.sessionInfo ? mapSessions(body, capacity) : previousEvent.sessionInfo,
        updatedAt: new Date().toISOString(),
        capacity,
        emptyCount: capacity,
      }
    }

    if (myPageShowIndex >= 0) {
      const previousShow = myPageShowsStore[myPageShowIndex]
      const nextVenueId = body.venueId ?? mockEventStore[eventIndex]?.venue.venueId
      const selectedVenue = mockVenues.find((venue) => venue.venueId === nextVenueId)

      myPageShowsStore[myPageShowIndex] = {
        ...previousShow,
        title: body.title ?? previousShow.title,
        posterUrl: previousShow.posterUrl,
        venue: selectedVenue?.name ?? previousShow.venue,
        purchaseLimit: body.purchaseLimit ?? previousShow.purchaseLimit,
        show: {
          showStartDate: body.showStartDate
            ? toDateOnly(body.showStartDate)
            : previousShow.show.showStartDate,
          showEndDate: body.showEndDate
            ? toDateOnly(body.showEndDate)
            : previousShow.show.showEndDate,
        },
        reservation: {
          startDate: body.reservationStartDate ?? previousShow.reservation.startDate,
          endDate: body.reservationEndDate ?? previousShow.reservation.endDate,
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
