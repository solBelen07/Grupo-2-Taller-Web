package com.tallerwebi.dominio.invitacion;

public enum Estado {
  PENDIENTE,
  ACEPTADA,
  RECHAZADA,
  // El usuario había aceptado y después salió del grupo (se guarda para no perder el historial).
  ABANDONADA,
}
