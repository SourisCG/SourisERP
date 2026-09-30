export class ApiRequestError extends Error {
  status: number;
  code?: string;

  constructor(status: number, message: string, code?: string) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: unknown;
};

export async function api<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`/api/backend/${path}`, {
    method: options.method ?? "GET",
    headers: options.body !== undefined ? { "content-type": "application/json" } : undefined,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  if (!response.ok) {
    let detail = response.statusText;
    let code: string | undefined;
    try {
      const problem = await response.json();
      detail = problem.detail ?? detail;
      code = problem.code;
    } catch {
      // not a problem+json body
    }
    throw new ApiRequestError(response.status, detail, code);
  }

  if (response.status === 204) {
    return undefined as T;
  }
  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) {
    return (await response.json()) as T;
  }
  return (await response.text()) as T;
}

export function backendUrl(path: string): string {
  return `/api/backend/${path}`;
}

export type PageMetadata = {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type Paged<T> = {
  content: T[];
  page: PageMetadata;
};
