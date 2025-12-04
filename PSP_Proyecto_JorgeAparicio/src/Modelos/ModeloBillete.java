package Modelos;

import java.util.Arrays;

public class ModeloBillete {

    // Atributos del Billete
    private int identificador;
    private String nombre;
    private byte[] firma;
    private String codCompra;
    private boolean disponible;

    // Constructor con los atributos
    public ModeloBillete(int indentificador,String nombre, byte[] firma,String codCompra, boolean disponible) {
        this.identificador = indentificador;
        this.nombre = nombre;
        this.firma = firma;
        this.codCompra = codCompra;
        this.disponible = disponible;
    }

    // Constructor vacio
    public ModeloBillete() {
    }

    // Metodos Getter & Setter
    public int getIdentificador() {
        return identificador;
    }

    public void setIdentificador(int identificador) {
        this.identificador = identificador;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public byte[] getFirma() {
        return firma;
    }

    public void setFirma(byte[] firma) {
        this.firma = firma;
    }

    public String getCodCompra() {
        return codCompra;
    }

    public void setCodCompra(String codCompra) {
        this.codCompra = codCompra;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    // Metodo ToString
    @Override
    public String toString() {
        return "ModeloBillete{" +
                "identificador=" + identificador +
                ", nombre='" + nombre + '\'' +
                ", firma=" + Arrays.toString(firma) +
                ", codCompra='" + codCompra + '\'' +
                ", disponible=" + disponible +
                '}';
    }
}
