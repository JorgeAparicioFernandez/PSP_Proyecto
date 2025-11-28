package Modelos;

public class ModeloCliente {

    // Atributos del Cliente
    private String nombre;
    private String apellido;
    private String edad;
    private String email;
    private String usuario;
    private String contra;

    // Constructor con los atributos
    public ModeloCliente(String nombre, String apellido, String edad, String email, String usuario, String contra) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.email = email;
        this.usuario = usuario;
        this.contra = contra;
    }

    // Constructor vacio
    public ModeloCliente() {
    }

    // Metodos Getter & Setter
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
        this.edad = edad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContra() {
        return contra;
    }

    public void setContra(String contra) {
        this.contra = contra;
    }

    // Metodo ToString
    @Override
    public String toString() {
        return "Cliente{" +
                "nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", edad=" + edad +
                ", email='" + email + '\'' +
                ", usuario='" + usuario + '\'' +
                ", contra='" + contra + '\'' +
                '}';
    }
}
