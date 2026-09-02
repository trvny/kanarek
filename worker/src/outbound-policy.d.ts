export interface OutboundPolicyEnv {
  ALLOWED_HOSTS?: string;
}

export declare function hostAllowed(host: string, env: OutboundPolicyEnv): boolean;
export declare function outboundUrlAllowed(url: URL, env: OutboundPolicyEnv): boolean;
export declare function assertOutboundUrlAllowed(target: string | URL, env: OutboundPolicyEnv): URL;
