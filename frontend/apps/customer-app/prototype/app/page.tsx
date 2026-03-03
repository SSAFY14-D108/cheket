export default function Home() {
  return (
    <section className="space-y-4">
      <div className="rounded-xl bg-secondary p-4">
        <p className="text-xs text-secondary-foreground/80">Welcome back</p>
        <h1 className="mt-1 text-xl font-semibold text-secondary-foreground">
          Check today&apos;s live events
        </h1>
      </div>

      <div className="space-y-3">
        <h2 className="text-sm font-medium text-muted-foreground">
          Featured shows
        </h2>
        <ul className="space-y-2">
          {["CHEKET FEST 2026", "SUMMER LIVE NIGHT", "INDIE MOON STAGE"].map(
            (show) => (
              <li
                key={show}
                className="rounded-xl border border-border bg-card p-4 text-sm"
              >
                <p className="font-medium text-card-foreground">{show}</p>
                <p className="mt-1 text-xs text-muted-foreground">
                  Seat selection and payment flow will be connected next.
                </p>
              </li>
            )
          )}
        </ul>
      </div>
    </section>
  );
}
