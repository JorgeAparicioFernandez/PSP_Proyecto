package Modelos;

import java.util.Arrays;

public class ModeloBillete {

    // Atributos del Billete
    private String nombre;
    private byte[] firma;

    // Constructor con los atributos
    public ModeloBillete(String nombre, byte[] firma) {
        this.nombre = nombre;
        this.firma = firma;
    }

    // Constructor vacio
    public ModeloBillete() {
    }

    // Metodos Getter & Setter
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

    // Metodo ToString
    @Override
    public String toString() {
        return "Billete{" +
                "nombre='" + nombre + '\'' +
                ", firma=" + Arrays.toString(firma) +
                '}';
    }

}
