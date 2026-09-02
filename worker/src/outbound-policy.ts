export interface OutboundPolicyEnv {
  ALLOWED_HOSTS?: string;
}

export function hostAllowed(host: string, env: OutboundPolicyEnv): boolean {
  const normalizedHost = host.toLowerCase().replace(/\.$/, "");
  if (
    !normalizedHost
    || normalizedHost === "localhost"
    || normalizedHost.endsWith(".localhost")
    || normalizedHost.endsWith(".local")
    || normalizedHost.endsWith(".internal")
    || normalizedHost.endsWith(".home")
    || normalizedHost.endsWith(".lan")
    || normalizedHost.includes(":")
    || /^\d{1,3}(?:\.\d{1,3}){3}$/.test(normalizedHost)
  ) return false;

  const allow = (env.ALLOWED_HOSTS || "").split(",").map((s) => s.trim()).filter(Boolean);
  return !allow.length || allow.some((raw) => {
    const suffix = raw.toLowerCase().replace(/^\./, "").replace(/\.$/, "");
    return suffix.length > 0 && (normalizedHost === suffix || normalizedHost.endsWith(`.${suffix}`));
  });
}

export function outboundUrlAllowed(url: URL, env: OutboundPolicyEnv): boolean {
  const defaultPort = url.protocol === "https:" ? "443" : url.protocol === "http:" ? "80" : "";
  return Boolean(defaultPort)
    && !url.username
    && !url.password
    && (!url.port || url.port === defaultPort)
    && hostAllowed(url.hostname, env);
}

export function assertOutboundUrlAllowed(target: string | URL, env: OutboundPolicyEnv): URL {
  const url = target instanceof URL ? target : new URL(target);
  if (!outboundUrlAllowed(url, env)) throw new Error("host not allowed");
  return url;
}
