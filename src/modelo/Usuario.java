package modelo; 
public class Usuario {
    private int id;
    private String nombre;
    private String correo;
    private String password;
    private int puntos;
    
    // Constructor Vacío
    public Usuario() {}

    // Constructor con parámetros
    public Usuario(int id, String nombre, String correo, String password, int puntos) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
        this.puntos = puntos;
    }
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }

}