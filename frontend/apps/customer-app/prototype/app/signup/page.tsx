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
    return step === 1
      ? "\uAE30\uBCF8 \uC815\uBCF4 \uC785\uB825"
      : "\uBE44\uBC00\uBC88\uD638 \uBC0F \uC57D\uAD00 \uB3D9\uC758";
  }, [step]);

  const handleSendCode = () => {
    if (phone.replace(/\D/g, "").length < 10) {
      setError("\uD734\uB300\uD3F0 \uBC88\uD638\uB97C \uC62C\uBC14\uB974\uAC8C \uC785\uB825\uD574\uC8FC\uC138\uC694.");
      return;
    }
    setCodeSent(true);
    setError("");
  };

  const handleVerifyCode = () => {
    if (code.length !== 6) {
      setError("\uC778\uC99D\uCF54\uB4DC 6\uC790\uB9AC\uB97C \uC785\uB825\uD574\uC8FC\uC138\uC694.");
      return;
    }
    setCodeVerified(true);
    setError("");
  };

  const handleNext = () => {
    if (!name.trim()) {
      setError("\uC774\uB984\uC744 \uC785\uB825\uD574\uC8FC\uC138\uC694.");
      return;
    }
    if (!codeVerified) {
      setError("SMS \uC778\uC99D\uC744 \uC644\uB8CC\uD574\uC8FC\uC138\uC694.");
      return;
    }
    setError("");
    setStep(2);
  };

  const handleSignup = () => {
    if (password.length < 6) {
      setError("\uBE44\uBC00\uBC88\uD638\uB294 6\uC790 \uC774\uC0C1\uC785\uB2C8\uB2E4.");
      return;
    }
    if (password !== passwordConfirm) {
      setError("\uBE44\uBC00\uBC88\uD638 \uD655\uC778\uAC12\uC774 \uC77C\uCE58\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4.");
      return;
    }
    if (!agreed) {
      setError("\uD544\uC218 \uC57D\uAD00 \uB3D9\uC758\uAC00 \uD544\uC694\uD569\uB2C8\uB2E4.");
      return;
    }
    setError(
      "\uD68C\uC6D0\uAC00\uC785 API\uB294 \uD604\uC7AC \uD50C\uB808\uC774\uC2A4\uD640\uB354\uB85C \uB3D9\uC791\uD569\uB2C8\uB2E4.",
    );
  };

  return (
    <section className="space-y-4">
      <header className="space-y-1">
        <h1 className="text-xl font-semibold text-foreground">
          {"\uD68C\uC6D0\uAC00\uC785"}
        </h1>
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
                {"\uC774\uB984"}
              </label>
              <input
                id="name"
                value={name}
                onChange={(e) => {
                  setName(e.target.value);
                  setError("");
                }}
                placeholder="\uC774\uB984 \uC785\uB825"
                className="h-10 w-full rounded-lg border border-border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring/40"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs text-muted-foreground" htmlFor="phone">
                {"\uD734\uB300\uD3F0 \uBC88\uD638"}
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
                  {codeSent ? "\uC7AC\uC804\uC1A1" : "\uCF54\uB4DC\uBC1C\uC1A1"}
                </button>
              </div>
            </div>

            {codeSent ? (
              <div className="space-y-1.5">
                <label className="text-xs text-muted-foreground" htmlFor="code">
                  {"SMS \uC778\uC99D\uCF54\uB4DC"}
                </label>
                <div className="flex gap-2">
                  <input
                    id="code"
                    value={code}
                    onChange={(e) => {
                      setCode(e.target.value);
                      setError("");
                    }}
                    placeholder="6\uC790\uB9AC \uC785\uB825"
                    className="h-10 flex-1 rounded-lg border border-border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring/40"
                  />
                  <button
                    type="button"
                    onClick={handleVerifyCode}
                    className="h-10 rounded-lg border border-border bg-secondary px-3 text-xs text-secondary-foreground"
                  >
                    {codeVerified ? "\uC644\uB8CC" : "\uC778\uC99D"}
                  </button>
                </div>
                <p className="text-[11px] text-muted-foreground">
                  {"SMS \uC778\uC99D\uC740 \uC2E4\uC81C \uC5F0\uB3D9 \uC804 \uD50C\uB808\uC774\uC2A4\uD640\uB354\uC785\uB2C8\uB2E4."}
                </p>
              </div>
            ) : null}

            <button
              type="button"
              onClick={handleNext}
              className="h-10 w-full rounded-lg bg-primary text-sm font-medium text-primary-foreground"
            >
              {"\uB2E4\uC74C"}
            </button>
          </>
        ) : (
          <>
            <div className="space-y-1.5">
              <label className="text-xs text-muted-foreground" htmlFor="password">
                {"\uBE44\uBC00\uBC88\uD638"}
              </label>
              <input
                id="password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  setError("");
                }}
                type="password"
                placeholder="6\uC790 \uC774\uC0C1 \uC785\uB825"
                className="h-10 w-full rounded-lg border border-border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-ring/40"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs text-muted-foreground" htmlFor="password-confirm">
                {"\uBE44\uBC00\uBC88\uD638 \uD655\uC778"}
              </label>
              <input
                id="password-confirm"
                value={passwordConfirm}
                onChange={(e) => {
                  setPasswordConfirm(e.target.value);
                  setError("");
                }}
                type="password"
                placeholder="\uBE44\uBC00\uBC88\uD638 \uB2E4\uC2DC \uC785\uB825"
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
              {"\uD544\uC218 \uC57D\uAD00(\uAC1C\uC778\uC815\uBCF4 \uCC98\uB9AC \uD3EC\uD568)\uC5D0 \uB3D9\uC758\uD569\uB2C8\uB2E4."}
            </button>

            <button
              type="button"
              onClick={handleSignup}
              className="h-10 w-full rounded-lg bg-primary text-sm font-medium text-primary-foreground"
            >
              {"\uAC00\uC785\uC644\uB8CC"}
            </button>
          </>
        )}

        {error ? <p className="text-xs text-destructive">{error}</p> : null}
      </div>

      <p className="text-center text-sm text-muted-foreground">
        {"\uC774\uBBF8 \uACC4\uC815\uC774 \uC788\uC73C\uC2E0\uAC00\uC694? "}
        <Link href="/login" className="font-medium text-primary">
          {"\uB85C\uADF8\uC778"}
        </Link>
      </p>
    </section>
  );
}
