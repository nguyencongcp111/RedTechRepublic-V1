// Keep the same key after an uncertain/network failure, including a page reload.
export function requestKey(scope: string, payload: unknown) {
  const fingerprint=JSON.stringify(payload), storageKey='pending-request:'+scope;
  const previous=sessionStorage.getItem(storageKey);
  if(previous){try{const saved=JSON.parse(previous);if(saved.fingerprint===fingerprint)return saved.key as string;}catch{/* replaced below */}}
  const key=crypto.randomUUID();sessionStorage.setItem(storageKey,JSON.stringify({fingerprint,key}));return key;
}
export function finishRequest(scope:string){sessionStorage.removeItem('pending-request:'+scope);}
