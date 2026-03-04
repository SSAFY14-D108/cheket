"use client";

import Link from "next/link";
import { ChevronRight, Copy, Heart, LogOut, Phone, User, Wallet } from "lucide-react";
import { useState } from "react";

const user = {
  name: "\uAE40\uBBFC\uC9C0",
  phone: "010-1234-5678",
  walletAddress: "0x3a9F...dE42",
  ctkBalance: 2400,
};

const menuItems = [
  "\uACF5\uC5F0 \uC608\uB9E4 \uB0B4\uC5ED",
  "\uACB0\uC81C \uC218\uB2E8 \uAD00\uB9AC",
  "\uC54C\uB9BC \uC124\uC815",
  "\uACE0\uAC1D\uC13C\uD130",
];

export default function MyPage() {
  const [copied, setCopied] = useState(false);

  const copyWalletAddress = async () => {
    try {
      await navigator.clipboard.writeText(user.walletAddress);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  };

  return (
    <section className="space-y-4 pb-3">
      <header>
        <h1 className="text-lg font-bold text-foreground">
          {"\uB9C8\uC774\uD398\uC774\uC9C0"}
        </h1>
      </header>

      <article className="rounded-xl border border-border bg-card p-5">
        <div className="mb-4 flex items-center gap-4">
          <div className="flex h-14 w-14 items-center justify-center rounded-full border border-primary/30 bg-primary/10">
            <User className="h-7 w-7 text-primary" />
          </div>
          <div>
            <p className="text-base font-semibold text-foreground">{user.name}</p>
            <p className="text-sm text-muted-foreground">CHEKET Member</p>
          </div>
        </div>

        <div className="space-y-2.5">
          <div className="flex items-center gap-2 text-sm">
            <Phone className="h-4 w-4 shrink-0 text-primary" />
            <span className="text-muted-foreground">{"\uD734\uB300\uD3F0"}</span>
            <span className="ml-auto font-medium text-foreground">{user.phone}</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <Wallet className="h-4 w-4 shrink-0 text-primary" />
            <span className="text-muted-foreground">{"\uC9C0\uAC11 \uC8FC\uC18C"}</span>
            <span className="ml-auto font-mono text-xs text-foreground">{user.walletAddress}</span>
            <button
              type="button"
              onClick={copyWalletAddress}
              aria-label="copy wallet address"
              className="text-muted-foreground transition-colors hover:text-foreground"
            >
              <Copy className="h-3.5 w-3.5" />
            </button>
          </div>
          {copied ? (
            <p className="text-right text-xs text-primary">
              {"\uC8FC\uC18C\uAC00 \uBCF5\uC0AC\uB418\uC5C8\uC2B5\uB2C8\uB2E4."}
            </p>
          ) : null}
        </div>
      </article>

      <article className="rounded-xl border border-primary/20 bg-primary/10 p-5">
        <p className="mb-1 text-xs text-muted-foreground">{"\uBCF4\uC720 CTK \uC794\uC561"}</p>
        <div className="flex items-end gap-2">
          <span className="text-3xl font-bold text-primary">{user.ctkBalance.toLocaleString()}</span>
          <span className="mb-0.5 text-sm text-muted-foreground">CTK</span>
        </div>
        <p className="mt-2 text-xs text-muted-foreground">
          {"\uC57D "}
          {(user.ctkBalance * 1.2).toLocaleString()}
          {"\uC6D0"}
        </p>
        <Link
          href="/wallet"
          className="mt-3 inline-flex rounded-lg bg-primary px-4 py-2 text-xs font-semibold text-primary-foreground hover:opacity-90"
        >
          {"\uC9C0\uAC11 \uAD00\uB9AC"}
        </Link>
      </article>

      <div className="grid grid-cols-3 gap-3">
        <div className="rounded-xl border border-border bg-card p-3 text-center">
          <p className="text-xl font-bold text-foreground">2</p>
          <p className="mt-0.5 text-xs text-muted-foreground">{"\uC608\uC57D \uD2F0\uCF13"}</p>
        </div>
        <div className="rounded-xl border border-border bg-card p-3 text-center">
          <p className="text-xl font-bold text-foreground">1</p>
          <p className="mt-0.5 text-xs text-muted-foreground">{"\uC591\uB3C4 \uC9C4\uD589 \uC911"}</p>
        </div>
        <Link
          href="/collection"
          className="rounded-xl border border-border bg-card p-3 text-center transition-all active:scale-[0.98] hover:border-primary/40"
        >
          <div className="flex items-center justify-center gap-1">
            <Heart className="h-4 w-4 text-primary" />
            <p className="text-xl font-bold text-foreground">3</p>
          </div>
          <p className="mt-0.5 text-xs text-muted-foreground">{"\uC704\uC2DC\uB9AC\uC2A4\uD2B8"}</p>
        </Link>
      </div>

      <article className="overflow-hidden rounded-xl border border-border bg-card">
        {menuItems.map((label) => (
          <button
            key={label}
            type="button"
            className="flex w-full items-center justify-between border-b border-border px-4 py-3.5 text-sm text-foreground last:border-none hover:bg-secondary"
          >
            {label}
            <ChevronRight className="h-4 w-4 text-muted-foreground" />
          </button>
        ))}
      </article>

      <button
        type="button"
        className="flex w-full items-center justify-center gap-2 rounded-xl border border-border bg-secondary py-3.5 text-sm font-semibold text-red-500 hover:border-red-300 hover:bg-red-50"
      >
        <LogOut className="h-4 w-4" />
        {"\uB85C\uADF8\uC544\uC6C3"}
      </button>
    </section>
  );
}
