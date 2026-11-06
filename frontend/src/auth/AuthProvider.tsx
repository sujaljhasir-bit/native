import React, {createContext, useContext, useRef, useState, useEffect} from 'react';
import {Platform} from 'react-native';
import {config} from './config';
export type User = {email:string; name:string; role:'ADMIN'|'USER'};
type Auth = {user:User|null; busy:boolean; error:string; login:(idToken:string)=>Promise<void>; logout:()=>Promise<void>; setError:(s:string)=>void};
const Context=createContext<Auth>(null!);
export const useAuth=()=>useContext(Context);
export function AuthProvider({children}:{children:React.ReactNode}) {
 const [user,setUser]=useState<User|null>(null), [busy,setBusy]=useState(false), [error,setError]=useState('');
 const token=useRef<string|null>(null); const expiry=useRef<ReturnType<typeof setTimeout>|null>(null);
 function route(path:string){if(Platform.OS==='web')window.history.replaceState({},'',path);}
 function clear(){token.current=null;setUser(null);if(expiry.current)clearTimeout(expiry.current);route('/');}
 useEffect(()=>{route('/');return()=>{if(expiry.current)clearTimeout(expiry.current)}},[]);
 useEffect(()=>{if(Platform.OS!=='web')return;const guard=()=>route(user?'/dashboard':'/');window.addEventListener('popstate',guard);return()=>window.removeEventListener('popstate',guard)},[user]);
 async function login(idToken:string){setBusy(true);setError('');try {
 const r=await fetch(config.api+'/api/auth/google',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({idToken}),signal:AbortSignal.timeout(15000)});
 if(!r.ok){const data=await r.json().catch(()=>({}));throw new Error(data.message || 'Unable to sign in. Please try again.')}
 const data=await r.json();token.current=data.token;
 const me=await fetch(config.api+'/api/me',{headers:{Authorization:'Bearer '+data.token},signal:AbortSignal.timeout(15000)});if(!me.ok)throw new Error('Session could not be verified. Please try again.');
 setUser(await me.json());route('/dashboard');expiry.current=setTimeout(()=>{clear();setError('Your session expired. Please sign in again.')},data.expiresIn*1000);
 }catch(e){clear();setError(e instanceof Error?e.message:'Connection failed. Please try again.')}finally{setBusy(false)}}
 async function logout(){setBusy(true);setError('');try{if(token.current){const r=await fetch(config.api+'/api/auth/logout',{method:'POST',headers:{Authorization:'Bearer '+token.current},signal:AbortSignal.timeout(10000)});if(!r.ok)throw new Error();}}catch{setError('Signed out on this device. Server revocation could not be confirmed; the session expires automatically.')}finally{clear();setBusy(false)}}
 return <Context.Provider value={{user,busy,error,login,logout,setError}}>{children}</Context.Provider>
}
