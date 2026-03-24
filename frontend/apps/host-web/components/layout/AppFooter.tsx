export function AppFooter() {
  return (
    <footer className="border-t border-black/10 bg-[#f3f4f6]">
      <div className="mx-auto flex max-w-[1160px] flex-col gap-6 px-5 py-6 sm:px-6">
        <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div className="space-y-2">
            <p className="text-[1.45rem] font-semibold tracking-[-0.04em] text-black">
              Host Workspace
            </p>
            <p className="max-w-[420px] text-sm leading-6 text-black/55">
              공연 등록부터 운영 관리까지 이어지는 호스트 전용
              워크스페이스입니다.
            </p>
          </div>
        </div>

        <div className="flex flex-col gap-3 border-t border-black/10 pt-4 text-xs text-black/50 sm:flex-row sm:items-center sm:justify-between">
          <p>© 2026 CHEKET D107</p>
          <p>NFT 기반 티켓 운영을 위한 호스트 관리 페이지</p>
        </div>
      </div>
    </footer>
  );
}
