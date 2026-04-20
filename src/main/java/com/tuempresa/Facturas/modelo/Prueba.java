package com.tuempresa.Facturas.modelo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.*;

public class Prueba {
    /*Esta clase fue hecha para las pruebas durante problemas de la
    * conexión de JDBC y java, se agregara una captura de la ceración de la
    * base de datos dentro de los archivos del proyecto como evidencia de la
    * conexión.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String dato;
}
