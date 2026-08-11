import { PageResponse } from '../models/employee.model';

/**
 * Builds a {@link PageResponse} for tests. Defaults describe a single, complete
 * page whose totals are derived from `items`, so a caller usually writes just
 * `pageOf()` (empty) or `pageOf([row])`; pass `overrides` for the rare case
 * that needs a specific page/limit/flag.
 */
export function pageOf<T>(items: T[] = [], overrides: Partial<PageResponse<T>> = {}): PageResponse<T> {
  return {
    items,
    page: 1,
    limit: 20,
    total: items.length,
    totalPages: items.length === 0 ? 0 : 1,
    hasNext: false,
    hasPrev: false,
    ...overrides,
  };
}
