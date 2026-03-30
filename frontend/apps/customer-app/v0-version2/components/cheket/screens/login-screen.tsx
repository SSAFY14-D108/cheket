"use client";

import { useState } from "react";
import { Eye, EyeOff, Lock, User } from "lucide-react";
import { useApp } from "@/lib/app-context";

export function LoginScreen() {
  const { login, navigate } = useApp();
  const [id, setId] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = () => {
    if (!id || !password) {
      setError("아이디와 비밀번호를 입력해 주세요.");
      return;
    }

    const ok = login(id, password);
    if (!ok) setError("로그인에 실패했어요. 입력한 정보를 다시 확인해 주세요.");
  };

  return (
    <div className="min-h-full bg-background">
      <div className="mx-auto min-h-full w-full max-w-[390px] overflow-hidden">
        <div className="relative h-[300px] overflow-hidden bg-[linear-gradient(135deg,rgba(226,218,255,0.95)_0%,rgba(196,247,224,0.92)_52%,rgba(202,230,255,0.95)_100%)]">
          <svg
            aria-hidden="true"
            viewBox="0 0 390 140"
            preserveAspectRatio="none"
            className="absolute bottom-0 left-0 h-[112px] w-full"
          >
            <path d="M0 102 C92 54 298 54 390 102 L390 140 L0 140 Z" fill="var(--background)" />
          </svg>
        </div>

        <div className="px-8 pb-8 pt-6">
          <h1 className="text-[28px] font-bold tracking-[-0.04em] text-[#111111]">로그인</h1>

          <div className="mt-8 flex flex-col gap-7">
            <label className="block">
              <div className="flex items-center gap-3 border-b border-[#d8efea] pb-3">
                <User className="h-4 w-4 text-[#5c7a73]" />
                <input
                  className="w-full bg-transparent text-sm text-[#111111] outline-none placeholder:text-[#7f9891]"
                  placeholder="아이디"
                  value={id}
                  onChange={(e) => {
                    setId(e.target.value);
                    setError("");
                  }}
                  autoComplete="username"
                />
              </div>
            </label>

            <label className="block">
              <div className="flex items-center gap-3 border-b border-[#d8efea] pb-3">
                <Lock className="h-4 w-4 text-[#5c7a73]" />
                <input
                  className="w-full bg-transparent text-sm text-[#111111] outline-none placeholder:text-[#7f9891]"
                  placeholder="비밀번호"
                  type={showPw ? "text" : "password"}
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    setError("");
                  }}
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPw(!showPw)}
                  className="text-[#5c7a73] transition hover:text-[#35554d]"
                  aria-label={showPw ? "비밀번호 숨기기" : "비밀번호 보기"}
                >
                  {showPw ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </label>
          </div>

          <div className="mt-5 flex items-center justify-end text-xs text-[#7f9891]">
            <button onClick={() => navigate("password-reset")} className="transition hover:text-[#35554d]">
              비밀번호 찾기
            </button>
          </div>

          {error ? <p className="mt-4 text-xs text-red-500">{error}</p> : null}

          <button
            onClick={handleLogin}
            className="gradient-border-button mt-8 w-full rounded-full py-3.5 text-sm font-semibold text-[#111111] transition active:scale-[0.99]"
          >
            로그인
          </button>

          <div className="mt-8 text-center text-xs text-[#7f9891]">
            <div className="flex items-center justify-center gap-2">
              <button onClick={() => navigate("find-account")} className="transition hover:text-[#35554d]">
                아이디 찾기
              </button>
              <span>·</span>
              <button onClick={() => navigate("signup")} className="font-semibold text-[#00c598] transition hover:text-[#009d7a]">
                회원가입
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
