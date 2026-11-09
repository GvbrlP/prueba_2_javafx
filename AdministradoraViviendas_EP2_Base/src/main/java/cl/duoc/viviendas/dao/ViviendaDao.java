package cl.duoc.viviendas.dao;

import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ViviendaDao {
    private final String ARCHIVO_DEPTOS = "departamentos.json";
    private final String ARCHIVO_CASAS = "casas.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void guardarDepartamentos(List<Departamento> departamentos) {
        try (FileWriter writer = new FileWriter(ARCHIVO_DEPTOS)) {
            gson.toJson(departamentos, writer);
        } catch (IOException e) {
            System.err.println("Error guardando departamentos: " + e.getMessage());
        }
    }

    public List<Departamento> leerDepartamentos() {
        try (FileReader reader = new FileReader(ARCHIVO_DEPTOS)) {
            Type tipoLista = new TypeToken<ArrayList<Departamento>>(){}.getType();
            List<Departamento> lista = gson.fromJson(reader, tipoLista);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void guardarCasas(List<Casa> casas) {
        try (FileWriter writer = new FileWriter(ARCHIVO_CASAS)) {
            gson.toJson(casas, writer);
        } catch (IOException e) {
            System.err.println("Error guardando casas: " + e.getMessage());
        }
    }

    public List<Casa> leerCasas() {
        try (FileReader reader = new FileReader(ARCHIVO_CASAS)) {
            Type tipoLista = new TypeToken<ArrayList<Casa>>(){}.getType();
            List<Casa> lista = gson.fromJson(reader, tipoLista);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}