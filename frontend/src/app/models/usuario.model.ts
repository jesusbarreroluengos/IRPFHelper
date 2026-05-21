/**
 * Modelo de datos que describe la estructura de un usuario gestionado por la
 * aplicación.
 */
export interface Usuario {
  id: number;
  usuario: string;
  email: string;
  password: string;
  esadmin: string;
  esverificado: string;
  numAccErroneos: number;
}
