package com.brazor.Server;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) throws IOException {
        System.out.println("Iniciando Servidor...");

        //Inicializar bd
        DataBaseService db = new DataBaseService();
        db.init();

    }
}