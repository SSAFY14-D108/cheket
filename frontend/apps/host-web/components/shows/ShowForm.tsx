"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { ArrowLeft, ImagePlus, Upload } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { DateTimePicker } from "@/components/common/DateTimePicker";
import { ApiError } from "@/lib/api";
import { formatDateTimeWithWeekday } from "@/lib/utils";
import { fetchMyWalletBalance } from "@/lib/mypage-api";
import { type HostShowDetail } from "@/lib/show-manage-api";
import { DescriptionEditor } from "./DescriptionEditor";
import { SettingsCardBasic } from "./SettingsCardBasic";
import { SettingsCardPolicies } from "./SettingsCardPolicies";
import { SettingsCardSessions } from "./SettingsCardSessions";
import { SettingsCardTickets } from "./SettingsCardTickets";
import {
  deriveReservationEndFromSessions,
  getReservationStartMinDate,
  PLATFORM_FEE_BPS,
  PLATFORM_TOTAL_BPS,
} from "./showFormUtils";
import { useShowForm } from "./useShowForm";

interface ShowFormProps {
  mode: "create" | "edit";
  initialData?: HostShowDetail;
}

const STEP_LABELS = ["기본 정보", "일정 / 장소", "티켓 설정", "정산 / 정책"];
const SETTLEMENT_CONFIRM_TEXT = "이해관계자의 수수료율 및 정책을 확인했습니다.";

function formatSharePercent(shareBps: string) {
  return `${((Number(shareBps) || 0) / 100).toLocaleString("ko-KR", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  })}%`;
}

function parseSectionCount(sectionId: string) {
  return sectionId
    .split(",")
    .map((value) => value.trim())
    .filter(Boolean).length;
}

export function ShowForm({ mode, initialData }: ShowFormProps) {
  const { toast } = useToast();
  const form = useShowForm({ mode, initialData });
  const {
    isEdit,
    isContentOnlyEdit,
    title,
    artistName,
    playtime,
    posterPreview,
    descriptionImagePreviews,
    description,
    venueId,
    venues,
    isLoadingVenues,
    venueLoadError,
    showStartAt,
    showEndAt,
    openAt,
    closeAt,
    purchaseLimit,
    grades,
    stakeholders,
    refundPolicy,
    sessionInfo,
    isSubmitting,
    setTitle,
    setArtistName,
    setPlaytime,
    setDescription,
    setPurchaseLimit,
    setOpenAt,
    handlePosterChange,
    handleDescriptionImagesChange,
    handleRemoveDescriptionImage,
    handleVenueChange,
    addGrade,
    removeGrade,
    updateGrade,
    addStakeholder,
    removeStakeholder,
    updateStakeholder,
    addRefund,
    removeRefund,
    updateRefund,
    addSession,
    removeSession,
    updateSession,
    getValidationMessage,
    handleSubmit,
  } = form;

  const [step, setStep] = useState(1);
  const [showStep1Errors, setShowStep1Errors] = useState(false);
  const [showStep2Errors, setShowStep2Errors] = useState(false);
  const [showStep3Errors, setShowStep3Errors] = useState(false);
  const [showStep4Errors, setShowStep4Errors] = useState(false);
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [isSettlementConfirmed, setIsSettlementConfirmed] = useState(false);
  const [walletStatus, setWalletStatus] = useState<
    "idle" | "loading" | "ready" | "error"
  >("idle");
  const [walletAddress, setWalletAddress] = useState("");
  const [walletBalance, setWalletBalance] = useState<number | null>(null);
  const [walletStatusMessage, setWalletStatusMessage] = useState("");
  const stepLabels = isContentOnlyEdit ? ["기본 정보"] : STEP_LABELS;
  const totalSteps = stepLabels.length;

  const headerTitle = isEdit ? "공연 수정" : "공연 등록";
  const submitLabel = isEdit ? "수정 완료" : "등록 완료";
  const isRemotePosterPreview =
    typeof posterPreview === "string" && /https?:\/\//.test(posterPreview);
  const canConfirmCreate = isSettlementConfirmed;
  const canEditStakeholders = !isEdit || !isContentOnlyEdit;
  const reservationOpenMinDate = useMemo(() => {
    if (!isEdit) {
      return getReservationStartMinDate();
    }

    if (openAt) {
      const currentReservationOpenDate = new Date(openAt);
      if (!Number.isNaN(currentReservationOpenDate.getTime())) {
        return currentReservationOpenDate;
      }
    }

    return undefined;
  }, [isEdit, openAt]);
  const reservationOpenMaxDate = useMemo(
    () => (showStartAt ? new Date(showStartAt) : undefined),
    [showStartAt],
  );
  const reservationDeadlineLabel = useMemo(() => {
    const calculatedCloseAt = closeAt || deriveReservationEndFromSessions(sessionInfo);

    return calculatedCloseAt
      ? formatDateTimeWithWeekday(calculatedCloseAt)
      : "마지막 회차를 선택하면 자동으로 계산됩니다.";
  }, [closeAt, sessionInfo]);
  const sessionMinDate = useMemo(() => getReservationStartMinDate(), []);
  const stakeholderShareBps = useMemo(
    () =>
      stakeholders
        .filter((item) => !item.isFixed)
        .reduce((sum, item) => sum + (Number(item.shareBps) || 0), 0),
    [stakeholders],
  );
  const settlementSummaryItems = useMemo(
    () =>
      stakeholders.map((item, index) => ({
        name: item.isFixed
          ? "플랫폼 수수료"
          : item.name.trim() || `참여자 ${index + 1}`,
        shareText: formatSharePercent(item.shareBps),
        isFixed: Boolean(item.isFixed),
      })),
    [stakeholders],
  );

  useEffect(() => {
    if (isEdit || !isConfirmOpen) return;
    let cancelled = false;
    const loadWallet = async () => {
      setWalletStatus("loading");
      setWalletStatusMessage("지갑 정보를 불러오는 중입니다.");
      try {
        const wallet = await fetchMyWalletBalance();
        if (cancelled) return;
        setWalletAddress(wallet.walletAddress ?? "");
        setWalletBalance(wallet.balance ?? null);
        setWalletStatus("ready");
        setWalletStatusMessage(
          wallet.walletAddress
            ? "등록 전 정산 지갑과 stakeholder NFT 정보를 확인해주세요."
            : "연결된 지갑 정보를 찾을 수 없습니다.",
        );
      } catch (error) {
        if (cancelled) return;
        setWalletStatus("error");
        setWalletAddress("");
        setWalletBalance(null);
        setWalletStatusMessage(
          error instanceof ApiError
            ? error.message
            : "지갑 정보를 불러오지 못했습니다.",
        );
      }
    };
    void loadWallet();
    return () => {
      cancelled = true;
    };
  }, [isConfirmOpen, isEdit]);

  const validateStep1 = () =>
    Boolean(
      title.trim() &&
      posterPreview &&
      description.trim() &&
      (isContentOnlyEdit ||
        (artistName.trim() && Number(playtime) > 0)),
    );
  const validateStep2 = () =>
    Boolean(
      venueId &&
      showStartAt &&
      showEndAt &&
      openAt &&
      closeAt &&
      sessionInfo.length > 0 &&
      sessionInfo.every(
        (session) => session.sessionDate && session.sessionStartTime,
      ),
    );
  const validateStep3 = () =>
    Boolean(
      Number(purchaseLimit) > 0 &&
      grades.length > 0 &&
      grades.every(
        (grade) =>
          grade.gradeName.trim() &&
          Number(grade.price) > 0 &&
          parseSectionCount(grade.sectionId) > 0,
      ),
    );

  const goNextStep = () => {
    if (isContentOnlyEdit) {
      return;
    }
    if (step === 1 && !validateStep1()) {
      setShowStep1Errors(true);
      return;
    }
    if (step === 2 && !validateStep2()) {
      setShowStep2Errors(true);
      return;
    }
    if (step === 3 && !validateStep3()) {
      setShowStep3Errors(true);
      return;
    }
    setStep((current) => Math.min(totalSteps, current + 1));
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handlePrimarySubmit = () => {
    const validationMessage = getValidationMessage();
    if (validationMessage) {
      setShowStep1Errors(true);
      setShowStep2Errors(true);
      setShowStep3Errors(true);
      setShowStep4Errors(true);
      toast({
        title: "입력값을 확인해주세요",
        description: validationMessage,
        variant: "destructive",
      });
      return;
    }
    if (isEdit) {
      void handleSubmit();
      return;
    }
    setIsSettlementConfirmed(false);
    setIsConfirmOpen(true);
  };

  return (
    <>
      <main className="min-h-svh bg-white pb-20">
        <div className="mx-auto max-w-6xl px-6 py-5">
          <div className="sticky top-0 z-40 mb-5 rounded-3xl border border-black/8 bg-white/92 px-4 py-4 shadow-sm backdrop-blur">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div className="flex items-center gap-3">
                <Link
                  href={
                    isEdit && initialData?.showId
                      ? `/shows/${initialData.showId}`
                      : "/mypage"
                  }
                  className="flex size-10 items-center justify-center rounded-full border border-black/10 bg-white transition-colors hover:bg-black/3"
                  aria-label="이전으로 이동"
                >
                  <ArrowLeft className="size-4" />
                </Link>
                <div>
                  <p className="text-sm font-semibold text-black">
                    {headerTitle}
                  </p>
                  <p className="text-xs text-black/45">
                    현재 단계 {step} / {totalSteps}
                  </p>
                </div>
              </div>
              <div className="flex w-full items-center gap-2 lg:max-w-xl">
                {stepLabels.map((label, index) => {
                  const current = index + 1;
                  const active = step === current;
                  const past = step > current;
                  return (
                    <button
                      key={`progress-${label}`}
                      type="button"
                      className={`flex flex-1 flex-col gap-2 text-left ${past ? "cursor-pointer hover:opacity-80" : ""}`}
                      onClick={() => past && setStep(current)}
                    >
                      <span
                        className={`text-[10px] sm:text-[11px] font-bold ${active ? "text-black" : past ? "text-black/65" : "text-black/28"}`}
                      >
                        {current}. {label}
                      </span>
                      <div className="h-1.5 w-full overflow-hidden rounded-full bg-black/6">
                        <div
                          className={`${past ? "w-full bg-black/35" : active ? "w-full bg-black" : "w-0 bg-transparent"} h-full transition-all duration-500 ease-out`}
                        />
                      </div>
                    </button>
                  );
                })}
              </div>
              <div className="flex items-center justify-end gap-2">
                {step > 1 && !isContentOnlyEdit ? (
                  <Button
                    variant="outline"
                    size="sm"
                    className="h-10 min-w-20 rounded-full border-black/10 bg-white font-bold"
                    onClick={() =>
                      setStep((current) => Math.max(1, current - 1))
                    }
                    disabled={isSubmitting}
                  >
                    이전
                  </Button>
                ) : null}
                {step < totalSteps && !isContentOnlyEdit ? (
                  <Button
                    size="sm"
                    className="h-10 min-w-20 rounded-full bg-black font-bold text-white shadow-none hover:bg-black/85"
                    onClick={goNextStep}
                  >
                    다음
                  </Button>
                ) : (
                  <Button
                    size="sm"
                    className="h-10 min-w-28 rounded-full bg-black font-bold text-white shadow-none hover:bg-black/85"
                    onClick={handlePrimarySubmit}
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? "저장 중..." : submitLabel}
                  </Button>
                )}
              </div>
            </div>
          </div>

          {step === 1 ? (
            <div className="mx-auto flex w-full max-w-6xl flex-col gap-4">
              <Card className="rounded-4xl border-black/8 bg-white py-0 shadow-sm">
                <div className="border-b border-black/8 px-6 pb-3 pt-5">
                  <h2 className="text-xl font-semibold tracking-[-0.03em] text-black">
                    기본 정보
                  </h2>
                  <p className="mt-1 text-sm text-black/45">
                    {isContentOnlyEdit
                      ? "최종 등록 이후에는 공연명, 포스터와 소개 정보만 수정할 수 있습니다."
                      : "포스터, 공연명, 아티스트명과 소개를 입력해주세요."}
                  </p>
                </div>
                <div className="p-5 sm:p-6">
                  <div className="flex flex-col gap-6 md:flex-row md:items-start">
                    <div className="flex w-full shrink-0 flex-col gap-3 sm:w-65">
                      <Label
                        className={`text-[15px] font-bold ${!posterPreview && showStep1Errors ? "text-destructive" : "text-foreground"}`}
                      >
                        포스터 이미지{" "}
                        <span className="text-destructive">*</span>
                      </Label>
                      <label
                        htmlFor="poster-upload"
                        className={`group relative flex aspect-3/4 w-full cursor-pointer flex-col items-center justify-center overflow-hidden rounded-3xl border-2 border-dashed transition-colors ${!posterPreview && showStep1Errors ? "border-destructive bg-destructive/5 hover:bg-destructive/10" : "border-black/10 bg-black/2 hover:bg-black/4"}`}
                      >
                        {posterPreview ? (
                          <>
                            <Image
                              src={posterPreview}
                              alt="포스터 미리보기"
                              fill
                              unoptimized={
                                posterPreview.startsWith("data:") ||
                                isRemotePosterPreview
                              }
                              className="object-cover transition-opacity group-hover:opacity-40"
                            />
                            <div className="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 transition-opacity group-hover:opacity-100">
                              <Button
                                type="button"
                                variant="secondary"
                                onClick={(event) => {
                                  event.preventDefault();
                                  document
                                    .getElementById("poster-upload")
                                    ?.click();
                                }}
                                className="font-semibold shadow-md"
                              >
                                <ImagePlus className="mr-2 size-4" />
                                포스터 교체
                              </Button>
                            </div>
                          </>
                        ) : (
                          <div className="flex flex-col items-center justify-center gap-3 p-4 text-muted-foreground">
                            <div className="rounded-full bg-white p-3.5 transition-colors group-hover:bg-black/5">
                              <Upload className="size-6" />
                            </div>
                            <div className="text-center">
                              <p className="font-bold text-foreground">
                                포스터 업로드
                              </p>
                              <p className="mt-1 text-[11px] font-medium text-muted-foreground">
                                권장 비율 3:4 / 최대 5MB
                              </p>
                            </div>
                          </div>
                        )}
                      </label>
                      <input
                        id="poster-upload"
                        type="file"
                        accept="image/jpeg,image/png,image/webp"
                        className="sr-only"
                        onChange={handlePosterChange}
                      />
                    </div>
                    <div className="flex w-full flex-1 flex-col gap-8">
                      <div className="flex flex-col gap-3">
                        <Label
                          className={`text-[15px] font-bold ${!title.trim() && showStep1Errors ? "text-destructive" : "text-foreground"}`}
                        >
                          공연명 <span className="text-destructive">*</span>
                        </Label>
                        <Input
                          placeholder="공연명을 입력해 주세요"
                          className={`h-14 rounded-2xl px-4 text-lg font-bold ${!title.trim() && showStep1Errors ? "border-destructive bg-destructive/5 focus-visible:ring-destructive" : "bg-black/2"}`}
                          value={title}
                          onChange={(event) => setTitle(event.target.value)}
                        />
                      </div>
                      {!isContentOnlyEdit ? (
                      <div className="flex flex-col gap-3">
                        <Label
                          className={`text-[15px] font-bold ${!artistName.trim() && showStep1Errors ? "text-destructive" : "text-foreground"}`}
                        >
                          아티스트 / 주최명{" "}
                          <span className="text-destructive">*</span>
                        </Label>
                        <Input
                          placeholder="표기할 아티스트 또는 주최명을 입력해 주세요"
                          className={`h-14 rounded-2xl px-4 text-lg font-bold ${!artistName.trim() && showStep1Errors ? "border-destructive bg-destructive/5 focus-visible:ring-destructive" : "bg-black/2"}`}
                          value={artistName}
                          onChange={(event) =>
                            setArtistName(event.target.value)
                          }
                        />
                      </div>
                      ) : null}
                      {!isContentOnlyEdit ? (
                      <div className="flex flex-col gap-3">
                        <Label
                          className={`text-[15px] font-bold ${(!playtime.trim() || Number(playtime) <= 0) && showStep1Errors ? "text-destructive" : "text-foreground"}`}
                        >
                          공연 시간(분){" "}
                          <span className="text-destructive">*</span>
                        </Label>
                        <Input
                          type="number"
                          placeholder="예: 120"
                          className={`h-14 rounded-2xl px-4 text-lg font-bold ${(!playtime.trim() || Number(playtime) <= 0) && showStep1Errors ? "border-destructive bg-destructive/5 focus-visible:ring-destructive" : "bg-black/2"}`}
                          value={playtime}
                          onChange={(event) => setPlaytime(event.target.value)}
                        />
                      </div>
                      ) : null}
                    </div>
                  </div>
                </div>
              </Card>
              <Card className="rounded-4xl border-black/8 bg-white py-0 shadow-sm">
                <div className="border-b border-black/8 px-6 py-4">
                  <h2 className="text-xl font-semibold tracking-[-0.03em] text-black">
                    상세 소개
                  </h2>
                  <p className="mt-1 text-sm text-black/45">
                    설명 이미지와 본문 내용을 작성해주세요.
                  </p>
                </div>
                <div className="p-5 sm:p-6">
                  <div className="flex flex-col gap-6 md:flex-row md:items-start">
                    <div className="flex w-full shrink-0 flex-col gap-3 sm:w-65">
                      <div className="flex items-center justify-between">
                        <Label className="text-[15px] font-bold text-foreground">
                          설명 이미지
                        </Label>
                        <span className="text-[11px] font-medium text-muted-foreground">
                          최대 50MB
                        </span>
                      </div>
                      <input
                        id="description-image-upload"
                        type="file"
                        accept="image/jpeg,image/png,image/webp"
                        multiple
                        className="sr-only"
                        onChange={handleDescriptionImagesChange}
                      />
                      <div className="grid grid-cols-2 gap-3 pt-1">
                        <div
                          className="group flex aspect-square cursor-pointer flex-col items-center justify-center gap-2 rounded-2xl border-2 border-dashed bg-black/2 text-muted-foreground transition-colors hover:bg-black/4"
                          onClick={() =>
                            document
                              .getElementById("description-image-upload")
                              ?.click()
                          }
                        >
                          <div className="rounded-full bg-white p-2.5 transition-colors group-hover:bg-black/5">
                            <ImagePlus className="size-5" />
                          </div>
                          <span className="text-[11px] font-bold text-foreground">
                            이미지 추가
                          </span>
                        </div>
                        {descriptionImagePreviews.map((preview, index) => (
                          <div
                            key={preview + index}
                            className="group relative aspect-square overflow-hidden rounded-2xl border bg-black/2 shadow-sm"
                          >
                            <Image
                              src={preview}
                              alt={`설명 이미지 ${index + 1}`}
                              fill
                              className="object-cover"
                              unoptimized={
                                preview.startsWith("data:") ||
                                preview.startsWith("http")
                              }
                            />
                            <div className="absolute inset-0 flex items-center justify-center bg-black/40 opacity-0 transition-opacity group-hover:opacity-100">
                              <button
                                type="button"
                                className="flex size-7 items-center justify-center rounded-full bg-destructive/90 text-white shadow-sm transition-colors hover:bg-destructive"
                                onClick={() =>
                                  handleRemoveDescriptionImage(index)
                                }
                              >
                                ×
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                    <div className="flex w-full flex-1 flex-col gap-3">
                      <Label
                        className={`text-[15px] font-bold ${!description.trim() && showStep1Errors ? "text-destructive" : "text-foreground"}`}
                      >
                        공연 설명 <span className="text-destructive">*</span>
                      </Label>
                      <div
                        className={`rounded-3xl border shadow-sm [&_.tiptap]:h-90 [&_.tiptap]:overflow-y-auto [&_.tiptap]:p-6 [&_.tiptap]:text-[15px] [&_.tiptap]:leading-relaxed ${!description.trim() && showStep1Errors ? "border-destructive bg-destructive/5" : "border-black/8"}`}
                      >
                        <DescriptionEditor
                          value={description}
                          onChange={setDescription}
                        />
                      </div>
                    </div>
                  </div>
                </div>
              </Card>
            </div>
          ) : null}

          {step === 2 && !isContentOnlyEdit ? (
            <div className="mx-auto flex w-full max-w-6xl flex-col gap-6">
              <SettingsCardBasic
                venueId={venueId}
                venues={venues}
                isLoadingVenues={isLoadingVenues}
                venueLoadError={venueLoadError}
                showErrors={showStep2Errors}
                onChangeVenueId={handleVenueChange}
              />
              <SettingsCardSessions
                sessionInfo={sessionInfo}
                reservationEndAt={closeAt}
                showStartAt={showStartAt}
                showEndAt={showEndAt}
                sessionMinDate={sessionMinDate}
                showErrors={showStep2Errors}
                onAddSession={addSession}
                onRemoveSession={removeSession}
                onUpdateSession={updateSession}
              />
              <Card className="rounded-4xl border-black/8 bg-white py-0 shadow-sm">
                <div className="border-b border-black/8 px-6 py-4">
                  <h2 className="text-xl font-semibold tracking-[-0.03em] text-black">
                    예매 일정
                  </h2>
                  {/* <p className="mt-1 text-sm text-black/45">공연장과 회차 정보를 확인한 뒤 예매 시작일을 설정해주세요.</p> */}
                </div>
                <div className="grid grid-cols-1 gap-6 px-5 pb-5 pt-4 sm:grid-cols-2 sm:px-6 sm:pb-6 sm:pt-4">
                  <div className="flex flex-col gap-2">
                    <Label
                      className={`text-[14px] font-bold ${!openAt && showStep2Errors ? "text-destructive" : "text-foreground"}`}
                    >
                      예매 시작일 <span className="text-destructive">*</span>
                    </Label>
                    <DateTimePicker
                      value={openAt || undefined}
                      onChange={setOpenAt}
                      placeholder="예매 시작 날짜/시간"
                      minDate={reservationOpenMinDate}
                      maxDate={reservationOpenMaxDate}
                    />
                    <p className="text-[11px] text-muted-foreground">
                      회차 선택 후 예매를 열 날짜와 시간을 정해주세요.
                    </p>
                    {!openAt && showStep2Errors ? (
                      <p className="text-[11px] font-medium text-destructive">
                        예매 시작일을 선택해주세요.
                      </p>
                    ) : null}
                  </div>
                  <div className="flex flex-col gap-2">
                    <Label
                      className={`text-[14px] font-bold ${(!closeAt || !showEndAt) && showStep2Errors ? "text-destructive" : "text-foreground"}`}
                    >
                      예매 마감일
                    </Label>
                    <div className="rounded-md bg-muted/20 px-4 py-3">
                      <div className="text-[15px] font-medium leading-relaxed text-foreground">
                        {reservationDeadlineLabel}
                      </div>
                    </div>
                    <p className="text-[11px] text-muted-foreground">
                      예매 마감일은 마지막 회차 공연 1시간 전으로 자동
                      계산됩니다.
                    </p>
                  </div>
                </div>
              </Card>
            </div>
          ) : null}
          {step === 3 && !isContentOnlyEdit ? (
            <SettingsCardTickets
              venueId={venueId}
              showStartAt={showStartAt}
              showEndAt={showEndAt}
              posterPreview={posterPreview}
              purchaseLimit={purchaseLimit}
              grades={grades}
              sessionInfo={sessionInfo}
              onChangePurchaseLimit={setPurchaseLimit}
              onAddGrade={addGrade}
              onRemoveGrade={removeGrade}
              onUpdateGrade={updateGrade}
              onAddSession={addSession}
              onRemoveSession={removeSession}
              onUpdateSession={updateSession}
              showErrors={showStep3Errors}
              showSessionSection={false}
            />
          ) : null}
          {step === 4 && !isContentOnlyEdit ? (
            <SettingsCardPolicies
              isEdit={isEdit}
              canEditStakeholders={canEditStakeholders}
              stakeholders={stakeholders}
              refundPolicy={refundPolicy}
              onAddStakeholder={addStakeholder}
              onRemoveStakeholder={removeStakeholder}
              onUpdateStakeholder={updateStakeholder}
              onAddRefund={addRefund}
              onRemoveRefund={removeRefund}
              onUpdateRefund={updateRefund}
              showErrors={showStep4Errors}
            />
          ) : null}
        </div>
      </main>

      <Dialog
        open={isConfirmOpen}
        onOpenChange={(open) => {
          setIsConfirmOpen(open);
          if (!open) setIsSettlementConfirmed(false);
        }}
      >
        <DialogContent className="sm:max-w-xl">
          <DialogHeader>
            <DialogTitle>정산 정책 확인</DialogTitle>
            <DialogDescription>
              공연 등록 전 정산 지갑과 분배 정책을 다시 한 번 확인해주세요.
            </DialogDescription>
          </DialogHeader>
          <div className="rounded-lg border border-border bg-muted/20 p-4">
            <div className="flex items-start justify-between gap-3">
              <div className="space-y-1">
                <p className="text-sm font-semibold text-foreground">
                  지갑 상태
                </p>
                <p className="text-sm text-muted-foreground">
                  {walletStatusMessage}
                </p>
              </div>
              <span
                className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-semibold ${walletStatus === "ready" ? "bg-emerald-100 text-emerald-700" : walletStatus === "error" ? "bg-destructive/10 text-destructive" : "bg-secondary text-secondary-foreground"}`}
              >
                {walletStatus === "ready"
                  ? "연결 완료"
                  : walletStatus === "error"
                    ? "연결 실패"
                    : "확인 중"}
              </span>
            </div>
            <div className="mt-3 grid gap-2 text-sm">
              <div className="flex items-center justify-between gap-3 rounded-md bg-background px-3 py-2">
                <span className="text-muted-foreground">지갑 주소</span>
                <span className="font-medium text-foreground">
                  {walletAddress
                    ? `${walletAddress.slice(0, 6)}...${walletAddress.slice(-4)}`
                    : "-"}
                </span>
              </div>
              <div className="flex items-center justify-between gap-3 rounded-md bg-background px-3 py-2">
                <span className="text-muted-foreground">잔액</span>
                <span className="font-medium text-foreground">
                  {walletBalance === null ? "-" : `${walletBalance} SSF`}
                </span>
              </div>
            </div>
          </div>
          <div className="space-y-2">
            <Label>수수료율 요약</Label>
            <div className="max-h-64 space-y-2 overflow-y-auto rounded-md border bg-background p-3">
              {settlementSummaryItems.map((item) => (
                <div
                  key={`${item.name}-${item.shareText}`}
                  className="flex items-center justify-between gap-3 rounded-md bg-muted/40 px-3 py-2 text-sm"
                >
                  <span
                    className={
                      item.isFixed
                        ? "font-semibold text-foreground"
                        : "text-foreground"
                    }
                  >
                    {item.name}
                  </span>
                  <span className="font-semibold text-foreground">
                    {item.shareText}
                  </span>
                </div>
              ))}
            </div>
          </div>
          <div className="space-y-2">
            <div className="rounded-lg border border-border bg-background px-4 py-3">
              <label
                htmlFor="settlement-confirm"
                className="flex cursor-pointer items-start gap-3"
              >
                <input
                  id="settlement-confirm"
                  type="checkbox"
                  checked={isSettlementConfirmed}
                  onChange={(event) =>
                    setIsSettlementConfirmed(event.target.checked)
                  }
                  className="mt-1 size-4 rounded border-black/20 text-black focus:ring-black"
                />
                <div className="space-y-1">
                  <p className="text-sm font-semibold text-foreground">
                    등록 전 확인
                  </p>
                  <p className="text-sm leading-6 text-muted-foreground">
                    {SETTLEMENT_CONFIRM_TEXT}
                  </p>
                </div>
              </label>
            </div>
          </div>
          <Button
            onClick={() => {
              if (!canConfirmCreate) return;
              setIsConfirmOpen(false);
              void handleSubmit();
            }}
            disabled={!canConfirmCreate || isSubmitting}
            className="h-11 w-full"
          >
            {isSubmitting ? "등록 중..." : "확인 후 등록"}
          </Button>
        </DialogContent>
      </Dialog>
    </>
  );
}

