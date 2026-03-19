'use client'

import { useEffect, useMemo, useState } from 'react'
import type { ISourceOptions } from '@tsparticles/engine'
import { initParticlesEngine } from '@tsparticles/react'
import Particles from '@tsparticles/react'
import { loadSlim } from '@tsparticles/slim'

let sparklesEngineReady = false

function buildSparkleOptions({
  count,
  direction,
  speed,
  sizeMin,
  sizeMax,
}: {
  count: number
  direction: 'none' | 'left' | 'right'
  speed: number
  sizeMin: number
  sizeMax: number
}): ISourceOptions {
  return {
    background: {
      color: 'transparent',
    },
    fullScreen: {
      enable: false,
    },
    detectRetina: true,
    fpsLimit: 60,
    particles: {
      number: {
        value: count,
        density: {
          enable: false,
        },
      },
      color: {
        value: ['#ffffff', '#f5f8ff', '#e6daff', '#cae6ff'],
      },
      shape: {
        type: ['star', 'circle'],
      },
      opacity: {
        value: { min: 0.18, max: 0.9 },
        animation: {
          enable: true,
          speed: 0.9,
          sync: false,
          startValue: 'random',
          destroy: 'none',
        },
      },
      size: {
        value: { min: sizeMin, max: sizeMax },
        animation: {
          enable: true,
          speed: 1.4,
          sync: false,
          startValue: 'random',
          destroy: 'none',
        },
      },
      move: {
        enable: true,
        speed,
        direction,
        random: true,
        straight: false,
        outModes: {
          default: 'out',
        },
      },
      twinkle: {
        particles: {
          enable: true,
          color: '#ffffff',
          frequency: 0.18,
          opacity: 1,
        },
      },
    },
    interactivity: {
      detectsOn: 'window',
      events: {
        onHover: {
          enable: false,
        },
        onClick: {
          enable: false,
        },
      },
    },
  }
}

export function TicketSparklesAura({ compact = false }: { compact?: boolean }) {
  const [ready, setReady] = useState(sparklesEngineReady)

  useEffect(() => {
    if (sparklesEngineReady) {
      return
    }

    void initParticlesEngine(async (engine) => {
      await loadSlim(engine)
    }).then(() => {
      sparklesEngineReady = true
      setReady(true)
    })
  }, [])

  const cornerOptions = useMemo(
    () => buildSparkleOptions({ count: compact ? 3 : 5, direction: 'none', speed: 0.14, sizeMin: 1.8, sizeMax: 4.2 }),
    [compact]
  )
  const sideOptions = useMemo(
    () => buildSparkleOptions({ count: compact ? 6 : 12, direction: 'none', speed: 0.12, sizeMin: 2.2, sizeMax: 5.4 }),
    [compact]
  )
  const bottomOptions = useMemo(
    () => buildSparkleOptions({ count: compact ? 5 : 10, direction: 'none', speed: 0.12, sizeMin: 2, sizeMax: 4.8 }),
    [compact]
  )

  if (!ready) return null

  return (
    <div className={`ticket-sparkles-aura${compact ? ' is-compact' : ''}`} aria-hidden="true">
      <Particles className="ticket-sparkles-aura__canvas ticket-sparkles-aura__canvas--top-left" options={cornerOptions} />
      <Particles className="ticket-sparkles-aura__canvas ticket-sparkles-aura__canvas--top-right" options={cornerOptions} />
      <Particles className="ticket-sparkles-aura__canvas ticket-sparkles-aura__canvas--mid-left" options={sideOptions} />
      <Particles className="ticket-sparkles-aura__canvas ticket-sparkles-aura__canvas--mid-right" options={sideOptions} />
      <Particles className="ticket-sparkles-aura__canvas ticket-sparkles-aura__canvas--bottom-left" options={bottomOptions} />
      <Particles className="ticket-sparkles-aura__canvas ticket-sparkles-aura__canvas--bottom-right" options={bottomOptions} />
    </div>
  )
}
