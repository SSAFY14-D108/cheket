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
      setError("아이디와 비밀번호를 입력해 주세요.");
      return;
    }
    setError("로그인 기능은 아직 플레이스홀더입니다.");
  };

  return (
    <section className="space-y-5">
      <header className="space-y-1">
        <h1 className="text-xl font-semibold text-foreground">로그인</h1>
        <p className="text-sm text-muted-foreground">
          CHEKET 계정으로 로그인하세요.
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
            placeholder="아이디"
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
            placeholder="비밀번호"
            className="h-11 w-full rounded-lg border border-border bg-background pl-10 pr-10 text-sm outline-none focus:ring-2 focus:ring-ring/40"
          />
          <button
            type="button"
            onClick={() => setShowPassword((prev) => !prev)}
            className="absolute top-1/2 right-3 -translate-y-1/2 text-muted-foreground"
            aria-label="비밀번호 표시 전환"
          >
            {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        </div>

        {error ? (
          <p className="text-xs text-destructive">{error}</p>
        ) : (
          <p className="text-xs text-muted-foreground">
            로그인 API 연동 전 화면/흐름 확인용입니다.
          </p>
        )}

        <button
          type="button"
          onClick={handleLogin}
          className="h-11 w-full rounded-lg bg-primary text-sm font-medium text-primary-foreground"
        >
          로그인
        </button>

        <Link
          href="/signup"
          className="block h-11 rounded-lg border border-border bg-secondary text-center text-sm leading-[44px] text-secondary-foreground"
        >
          회원가입
        </Link>
      </div>
    </section>
  );
}
