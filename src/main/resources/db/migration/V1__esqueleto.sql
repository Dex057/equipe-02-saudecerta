-- Migração inicial do esqueleto (sem tabelas).
-- Cada módulo cria as suas tabelas numa faixa própria de versões, para as branches não colidirem:
--   cadastro    V10 a V19  (prefixo cad_)
--   agenda      V20 a V29  (prefixo age_)
--   prontuario  V30 a V39  (prefixo pro_)
--   seguranca   V40 a V49  (prefixo seg_)
--   auditoria   V50 a V59  (prefixo aud_)
-- Convenção de nomes e privilégios: ADR-006.
SELECT 1;
