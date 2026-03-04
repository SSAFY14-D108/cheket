"use client";

import Link from "next/link";
import { Eye, EyeOff, Lock, User } from "lucide-react";
import { useState } from "react";

export default function LoginPage() {
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = () => {
    if (!userId.trim() || !password.trim()) {
      setError("\uC544\uC774\uB514\uC640 \uBE44\uBC00\uBC88\uD638\uB97C \uBAA8\uB450 \uC785\uB825\uD574\uC8FC\uC138\uC694.");
      return;
    }
    setError(
      "\uB85C\uADF8\uC778 API\uB294 \uC544\uC9C1 \uC5F0\uACB0\uB418\uC9C0 \uC54A\uC740 \uD50C\uB808\uC774\uC2A4\uD640\uB354\uC785\uB2C8\uB2E4.",
    );
  };

  return (
    <section className="space-y-5">
      <header className="space-y-1">
        <h1 className="text-xl font-semibold text-foreground">
          {"\uB85C\uADF8\uC778"}
        </h1>
        <p className="text-sm text-muted-foreground">
          {"CHEKET \uACC4\uC815\uC73C\uB85C \uB85C\uADF8\uC778\uD574\uC8FC\uC138\uC694."}
        </p>
      </header>

      <div className="space-y-3 rounded-xl border border-border bg-card p-4">
        <div className="relative">
          <User
            size={16}
            className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-muted-foreground"
          />
          <input
            value={userId}
            onChange={(e) => {
              setUserId(e.target.value);
              setError("");
            }}
            placeholder="\uC544\uC774\uB514"
            className="h-11 w-full rounded-lg border border-border bg-background pl-10 pr-3 text-sm outline-none focus:ring-2 focus:ring-ring/40"
          />
        </div>

        <div className="relative">
          <Lock
            size={16}
            className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-muted-foreground"
          />
          <input
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              setError("");
            }}
            type={showPassword ? "text" : "password"}
            placeholder="\uBE44\uBC00\uBC88\uD638"
            className="h-11 w-full rounded-lg border border-border bg-background pl-10 pr-10 text-sm outline-none focus:ring-2 focus:ring-ring/40"
          />
          <button
            type="button"
            onClick={() => setShowPassword((prev) => !prev)}
            className="absolute top-1/2 right-3 -translate-y-1/2 text-muted-foreground"
            aria-label="\uBE44\uBC00\uBC88\uD638 \uD45C\uC2DC \uC804\uD658"
          >
            {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        </div>

        {error ? (
          <p className="text-xs text-destructive">{error}</p>
        ) : (
          <p className="text-xs text-muted-foreground">
            {"\uB85C\uADF8\uC778\uC740 \uD604\uC7AC \uD504\uB85C\uD1A0\uD0C0\uC785 \uD50C\uB808\uC774\uC2A4\uD640\uB354 \uC0C1\uD0DC\uC785\uB2C8\uB2E4."}
          </p>
        )}

        <button
          type="button"
          onClick={handleLogin}
          className="h-11 w-full rounded-lg bg-primary text-sm font-medium text-primary-foreground"
        >
          {"\uB85C\uADF8\uC778"}
        </button>

        <Link
          href="/signup"
          className="block h-11 rounded-lg border border-border bg-secondary text-center text-sm leading-[44px] text-secondary-foreground"
        >
          {"\uD68C\uC6D0\uAC00\uC785"}
        </Link>
      </div>
    </section>
  );
}
