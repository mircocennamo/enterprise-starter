package it.interno.platform.starter.core.commons;



import java.util.List;


public record TokenInfo (
     String iss,
     List<String> aud,
     Long exp,
     String jti,
     Long iat,
     String sub,
     String client,
     List<String> scope,
     String domain,
     String grant,
     String ufficio,
     String codufficio,
     String cognome,
     String appid,
     String nome,
     String osid,
     String idmotivazione,
     String gruppi,
     Long rem_exp,
     String rt_id
){}
