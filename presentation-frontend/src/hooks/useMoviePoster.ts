import { useEffect, useState } from 'react'
import { fetchMovieImages, type TmdbResult } from '../lib/tmdb'

const EMPTY: TmdbResult = { posterUrl: null, backdropUrl: null }

export function useMoviePoster(titulo: string): TmdbResult {
  const [result, setResult] = useState<TmdbResult>(() => {
    // tenta carregar do cache de forma síncrona para evitar flash
    try {
      const raw = localStorage.getItem(`tmdb:${titulo.trim().toLowerCase()}`)
      if (raw) {
        const entry = JSON.parse(raw)
        return { posterUrl: entry.posterUrl, backdropUrl: entry.backdropUrl }
      }
    } catch {}
    return EMPTY
  })

  useEffect(() => {
    if (!titulo) return
    fetchMovieImages(titulo).then(setResult).catch(() => {})
  }, [titulo])

  return result
}
