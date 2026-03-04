"use client";

import Link from "next/link";
import { Check } from "lucide-react";
import { useMemo, useState } from "react";

type Step = 1 | 2;

export default function SignupPage() {
  const [step, setStep] = useState<Step>(1);
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [code, setCode] = useState("");
  const [codeSent, setCodeSent] = useState(false);
  const [codeVerified, setCodeVerified] = useState(false);
  const [password, setPassword] = useState("");
  const [passwordConfirm, setPasswordConfirm] = useState("");
  const [agreed, setAgreed] = useState(false);
  const [error, setError] = useState("");

  const stepTitle = useMemo(() => {
    return step === 1 ? "기본 정보 입력" : "비밀번호 및 약관 동의";
  }, [step]);

  const handleSendCode = () => {
    if (phone.replace(/\D/g, "").length < 10) {
      setError("휴대폰 번호를 정확히 입력해 주세요.");
      return;
    }
    setCodeSent(true);
    setError("");
  };

  const handleVerifyCode = () => {
    if (code.length !== 6) {
      setError("인증번호 6자리를 입력해 주세요.");
      return;
    }
    setCodeVerified(true);
    setError("");
  };

  const handleNext = () => {
    if (!name.trim()) {
      setError("이름을 입력해 주세요.");
      return;
    }
    if (!codeVerified) {
      setError("SMS 인증을 완료해 주세요.");
      return;
    }
    setError("");
    setStep(2);
  };

  const handleSignup = () => {
    if (password.length < 6) {
      setError("비밀번호는 6자 이상이어야 합니다.");
      return;
    }
    if (password !== passwordConfirm) {
      setError("비밀번호 확인이 일치하지 않습니다.");
      return;
    }
    if (!agreed) {
      setError("약관에 동의해 주세요.");
      return;
    }
    setError("회원가입 기능은 아직 플레이스홀더입니다.");
  };

  return (
    <section className="space-y-4">
      <header className="space-y-1">
        <h1 className="text-xl font-semibold text-foreground">회원가입</h1>
        <p className="text-sm text-muted-foreground">{stepTitle}</p>
      </header>

      <div className="flex gap-2">
        <div
          className={`h-1 flex-1 rounded-full ${step >= 1 ? "bg-primary" : "bg-secondary"}`}
        />
        <div
          className={`h-1 flex-1 rounded-full ${step >= 2 ? "bg-primary" : "bg-secondary"}`}
        />
      </div>

      <div className="space-y-3 rounded-xl border border-border bg-card p-4">
        {step === 1 ? (
          <>
            <div className="space-y-1.5">
              <label className="text-xs text-muted-foreground" htmlFor="name">
                이름
              </label>
              <input
                id="name"
                value={name}
                onChange={(e) => {
                  setName(e.target.value);
                  setError("");
                }}
                placeholder="이름 입력"
                className="h-10 w-full rounded-lg border border-border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring/40"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs text-muted-foreground" htmlFor="phone">
                휴대폰 번호
              </label>
              <div className="flex gap-2">
                <input
                  id="phone"
                  value={phone}
                  onChange={(e) => {
                    setPhone(e.target.value);
                    setError("");
                  }}
                  placeholder="010-0000-0000"
                  className="h-10 flex-1 rounded-lg border border-border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring/40"
                />
                <button
                  type="button"
                  onClick={handleSendCode}
                  className="h-10 rounded-lg border border-border bg-secondary px-3 text-xs text-secondary-foreground"
                >
                  {codeSent ? "재요청" : "인증요청"}
                </button>
              </div>
            </div>

            {codeSent ? (
              <div className="space-y-1.5">
                <label className="text-xs text-muted-foreground" htmlFor="code">
                  인증번호
                </label>
                <div className="flex gap-2">
                  <input
                    id="code"
                    value={code}
                    onChange={(e) => {
                      setCode(e.target.value);
                      setError("");
                    }}
                    placeholder="6자리 입력"
                    className="h-10 flex-1 rounded-lg border border-border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring/40"
                  />
                  <button
                    type="button"
                    onClick={handleVerifyCode}
                    className="h-10 rounded-lg border border-border bg-secondary px-3 text-xs text-secondary-foreground"
                  >
                    {codeVerified ? "확인됨" : "인증확인"}
                  </button>
                </div>
                <p className="text-[11px] text-muted-foreground">
                  SMS 인증은 플레이스홀더이며 실제 전송/검증은 연결되지 않았습니다.
                </p>
              </div>
            ) : null}

            <button
              type="button"
              onClick={handleNext}
              className="h-10 w-full rounded-lg bg-primary text-sm font-medium text-primary-foreground"
            >
              다음
            </button>
          </>
        ) : (
          <>
            <div className="space-y-1.5">
              <label
                className="text-xs text-muted-foreground"
                htmlFor="password"
              >
                비밀번호
              </label>
              <input
                id="password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  setError("");
                }}
                type="password"
                placeholder="6자 이상 입력"
                className="h-10 w-full rounded-lg border border-border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring/40"
              />
            </div>

            <div className="space-y-1.5">
              <label
                className="text-xs text-muted-foreground"
                htmlFor="password-confirm"
              >
                비밀번호 확인
              </label>
              <input
                id="password-confirm"
                value={passwordConfirm}
                onChange={(e) => {
                  setPasswordConfirm(e.target.value);
                  setError("");
                }}
                type="password"
                placeholder="비밀번호 재입력"
                className="h-10 w-full rounded-lg border border-border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring/40"
              />
            </div>

            <button
              type="button"
              onClick={() => setAgreed((prev) => !prev)}
              className="flex items-center gap-2 rounded-lg border border-border bg-secondary px-3 py-2 text-left text-xs text-foreground"
            >
              <span
                className={`flex h-4 w-4 items-center justify-center rounded-full border ${
                  agreed ? "border-primary bg-primary text-primary-foreground" : "border-border"
                }`}
              >
                {agreed ? <Check size={12} /> : null}
              </span>
              필수 약관(서비스/개인정보 처리)에 동의합니다.
            </button>

            <button
              type="button"
              onClick={handleSignup}
              className="h-10 w-full rounded-lg bg-primary text-sm font-medium text-primary-foreground"
            >
              가입하기
            </button>
          </>
        )}

        {error ? <p className="text-xs text-destructive">{error}</p> : null}
      </div>

      <p className="text-center text-sm text-muted-foreground">
        이미 계정이 있나요?{" "}
        <Link href="/login" className="font-medium text-primary">
          로그인
        </Link>
      </p>
    </section>
  );
}
