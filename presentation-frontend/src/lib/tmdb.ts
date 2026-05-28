const TOKEN = import.meta.env.VITE_TMDB_TOKEN as string
const BASE_IMG = 'https://image.tmdb.org/t/p'
const CACHE_TTL = 7 * 24 * 60 * 60 * 1000 // 7 dias

export type TmdbResult = {
  posterUrl: string | null   // vertical — bom pra cards (w342)
  backdropUrl: string | null // horizontal — bom pra banners (w1280)
}

type CacheEntry = TmdbResult & { ts: number }

function cacheKey(titulo: string) {
  return `tmdb:${titulo.trim().toLowerCase()}`
}

function fromCache(titulo: string): TmdbResult | null {
  try {
    const raw = localStorage.getItem(cacheKey(titulo))
    if (!raw) return null
    const entry: CacheEntry = JSON.parse(raw)
    if (Date.now() - entry.ts > CACHE_TTL) { localStorage.removeItem(cacheKey(titulo)); return null }
    return { posterUrl: entry.posterUrl, backdropUrl: entry.backdropUrl }
  } catch { return null }
}

function toCache(titulo: string, result: TmdbResult) {
  try {
    localStorage.setItem(cacheKey(titulo), JSON.stringify({ ...result, ts: Date.now() }))
  } catch {}
}

export async function fetchMovieImages(titulo: string): Promise<TmdbResult> {
  const cached = fromCache(titulo)
  if (cached) return cached

  const url = `https://api.themoviedb.org/3/search/movie?query=${encodeURIComponent(titulo)}&language=pt-BR&include_adult=false&page=1`
  const res = await fetch(url, {
    headers: { Authorization: `Bearer ${TOKEN}`, accept: 'application/json' },
  })

  if (!res.ok) return { posterUrl: null, backdropUrl: null }

  const data = await res.json()
  const movie = data.results?.[0]

  const result: TmdbResult = {
    posterUrl: movie?.poster_path ? `${BASE_IMG}/w342${movie.poster_path}` : null,
    backdropUrl: movie?.backdrop_path ? `${BASE_IMG}/w1280${movie.backdrop_path}` : null,
  }

  toCache(titulo, result)
  return result
}
