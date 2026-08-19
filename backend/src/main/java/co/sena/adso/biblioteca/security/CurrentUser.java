package co.sena.adso.biblioteca.security;

import co.sena.adso.biblioteca.entity.RolUsuario;

/**
 * Usuario autenticado expuesto como principal de Spring Security.
 */
public record CurrentUser(Long id, String correo, RolUsuario rol) {

    public boolean isRole(RolUsuario other) {
        return rol == other;
    }

    public boolean hasAnyRole(RolUsuario... roles) {
        for (RolUsuario r : roles) {
            if (rol == r) {
                return true;
            }
        }
        return false;
    }
}
