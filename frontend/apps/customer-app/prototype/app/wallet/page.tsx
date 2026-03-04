"use client";

import { ArrowDownLeft, ArrowUpRight, CheckCircle2, Copy, Plus, Wallet } from "lucide-react";
import { useState } from "react";

const topUpAmounts = [5000, 10000, 30000, 50000];

const transactions = [
  { id: "tx_001", type: "in", label: "CTK \uCDA9\uC804", amount: 3000, date: "2026.02.15" },
  { id: "tx_002", type: "out", label: "AESPA WORLD TOUR 2026 - R\uC11D", amount: 140000, date: "2026.02.10" },
  { id: "tx_003", type: "in", label: "CTK \uCDA9\uC804", amount: 5000, date: "2026.01.28" },
  { id: "tx_004", type: "out", label: "ULTRA KOREA 2026 - VIP", amount: 300000, date: "2026.01.20" },
];

const initialBalance = 2400;
const walletAddress = "0x3a9F...dE42";

export default function WalletPage() {
  const [balance, setBalance] = useState(initialBalance);
  const [copied, setCopied] = useState(false);
  const [topping, setTopping] = useState(false);
  const [successAmount, setSuccessAmount] = useState<number | null>(null);

  const copyAddress = async () => {
    try {
      await navigator.clipboard.writeText(walletAddress);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  };

  const handleTopUp = (amount: number) => {
    setTopping(true);
    setTimeout(() => {
      setBalance((prev) => prev + amount);
      setSuccessAmount(amount);
      setTopping(false);
      setTimeout(() => setSuccessAmount(null), 2200);
    }, 650);
  };

  return (
    <section className="space-y-4 pb-3">
      <header>
        <h1 className="text-lg font-bold text-foreground">{"\uC9C0\uAC11"}</h1>
      </header>

      <article className="rounded-2xl border border-primary/20 bg-gradient-to-br from-primary/20 to-primary/5 p-5">
        <div className="mb-4 flex items-center gap-2">
          <Wallet className="h-5 w-5 text-primary" />
          <span className="text-sm font-semibold text-foreground">CTK {"\uC794\uC561"}</span>
        </div>

        <div className="mb-1 flex items-end gap-2">
          <span className="text-4xl font-bold text-foreground">{balance.toLocaleString()}</span>
          <span className="mb-1 text-base text-muted-foreground">CTK</span>
        </div>
        <p className="text-sm text-muted-foreground">
          {"\uC57D "}
          {(balance * 1.2).toLocaleString()}
          {"\uC6D0"}
        </p>

        <div className="mt-4 border-t border-primary/10 pt-4">
          <p className="mb-1.5 text-xs text-muted-foreground">{"\uC9C0\uAC11 \uC8FC\uC18C"}</p>
          <div className="flex items-center justify-between gap-2 rounded-lg bg-background/40 px-3 py-2">
            <span className="truncate font-mono text-xs text-foreground">{walletAddress}</span>
            <button
              type="button"
              onClick={copyAddress}
              className="shrink-0 text-muted-foreground transition-colors hover:text-foreground"
              aria-label="copy wallet address"
            >
              {copied ? (
                <CheckCircle2 className="h-4 w-4 text-primary" />
              ) : (
                <Copy className="h-4 w-4" />
              )}
            </button>
          </div>
          {copied ? (
            <p className="mt-1 text-xs text-primary">
              {"\uC8FC\uC18C\uAC00 \uBCF5\uC0AC\uB418\uC5C8\uC2B5\uB2C8\uB2E4."}
            </p>
          ) : null}
        </div>
      </article>

      <article className="rounded-xl border border-border bg-card p-4">
        <h2 className="mb-3 text-sm font-semibold text-foreground">{"\uC989\uC2DC CTK \uCDA9\uC804"}</h2>
        <div className="grid grid-cols-2 gap-2">
          {topUpAmounts.map((amount) => (
            <button
              key={amount}
              type="button"
              onClick={() => handleTopUp(amount)}
              disabled={topping}
              className="flex items-center justify-center gap-1.5 rounded-xl border border-border bg-secondary py-3 text-sm font-semibold text-foreground transition-all hover:border-primary/50 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50"
            >
              <Plus className="h-3.5 w-3.5" />
              {amount.toLocaleString()} CTK
            </button>
          ))}
        </div>

        {successAmount !== null ? (
          <div className="mt-3 flex items-center justify-center gap-2 rounded-xl border border-primary/20 bg-primary/10 py-2.5">
            <CheckCircle2 className="h-4 w-4 text-primary" />
            <p className="text-sm font-medium text-primary">
              +{successAmount.toLocaleString()} CTK {"\uCDA9\uC804 \uC644\uB8CC"}
            </p>
          </div>
        ) : null}
      </article>

      <article className="overflow-hidden rounded-xl border border-border bg-card">
        <div className="border-b border-border px-4 py-3">
          <h2 className="text-sm font-semibold text-foreground">{"\uAC70\uB798 \uB0B4\uC5ED"}</h2>
        </div>
        <div className="divide-y divide-border">
          {transactions.map((tx) => (
            <div key={tx.id} className="flex items-center gap-3 px-4 py-3">
              <div
                className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full ${
                  tx.type === "in" ? "bg-primary/10" : "bg-secondary"
                }`}
              >
                {tx.type === "in" ? (
                  <ArrowDownLeft className="h-4 w-4 text-primary" />
                ) : (
                  <ArrowUpRight className="h-4 w-4 text-muted-foreground" />
                )}
              </div>
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium text-foreground">{tx.label}</p>
                <p className="text-xs text-muted-foreground">{tx.date}</p>
              </div>
              <span
                className={`shrink-0 text-sm font-semibold ${
                  tx.type === "in" ? "text-primary" : "text-foreground"
                }`}
              >
                {tx.type === "in" ? "+" : "-"}
                {tx.amount.toLocaleString()}
                <span className="ml-0.5 text-xs font-normal text-muted-foreground">CTK</span>
              </span>
            </div>
          ))}
        </div>
      </article>
    </section>
  );
}
