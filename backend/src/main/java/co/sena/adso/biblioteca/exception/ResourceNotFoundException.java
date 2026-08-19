package co.sena.adso.biblioteca.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " con el ID " + id + " no fue encontrado en la base de datos.");
    }
}
