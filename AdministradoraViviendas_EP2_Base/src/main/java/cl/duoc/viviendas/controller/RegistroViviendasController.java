package cl.duoc.viviendas.controller;

import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Vivienda;
import cl.duoc.viviendas.dao.ViviendaDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class RegistroViviendasController {

    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtSuperficie;
    @FXML private TextField txtHabitaciones;
    @FXML private TextField txtPiso;
    @FXML private CheckBox chkGastoComun;
    @FXML private CheckBox chkEstacionamiento;
    @FXML private CheckBox chkPatio;

    @FXML private TableView<Vivienda> tablaViviendas;
    @FXML private TableColumn<Vivienda, String> colTipo;
    @FXML private TableColumn<Vivienda, String> colCodigo;
    @FXML private TableColumn<Vivienda, Double> colSuperficie;
    @FXML private TableColumn<Vivienda, Integer> colHabitaciones;
    @FXML private TableColumn<Vivienda, Integer> colCosto;

    @FXML private Label lblMensajes;

    private ViviendaDao dao = new ViviendaDao();
    private ObservableList<Vivienda> listaViviendas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cmbTipo.setItems(FXCollections.observableArrayList("Departamento", "Casa"));

        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colSuperficie.setCellValueFactory(new PropertyValueFactory<>("superficie"));
        colHabitaciones.setCellValueFactory(new PropertyValueFactory<>("habitaciones"));
        colCosto.setCellValueFactory(cellData -> {
            Vivienda v = cellData.getValue();
            double costo = 0.0;
            if (v != null) {
                costo = v.calcularCostoArriendo();
            }
            return new javafx.beans.property.SimpleObjectProperty<>((int)costo);
        });

        colCosto.setCellFactory(column -> new javafx.scene.control.TableCell<Vivienda, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,d", item).replace(',', '.'));
                }
            }
        });

        listaViviendas.addAll(dao.leerDepartamentos());
        listaViviendas.addAll(dao.leerCasas());
        tablaViviendas.setItems(listaViviendas);

        cambiarTipoVivienda();
    }

    @FXML
    public void cambiarTipoVivienda() {
        String tipoSeleccionado = cmbTipo.getValue();
        if ("Departamento".equals(tipoSeleccionado)) {
            txtPiso.setDisable(false);
            chkGastoComun.setDisable(false);
            chkEstacionamiento.setDisable(false);
            chkPatio.setDisable(true);
            chkPatio.setSelected(false);
        } else if ("Casa".equals(tipoSeleccionado)) {
            txtPiso.setDisable(true);
            txtPiso.clear();
            chkGastoComun.setDisable(true);
            chkGastoComun.setSelected(false);
            chkEstacionamiento.setDisable(true);
            chkEstacionamiento.setSelected(false);
            chkPatio.setDisable(false);
        } else {
            txtPiso.setDisable(true);
            chkGastoComun.setDisable(true);
            chkEstacionamiento.setDisable(true);
            chkPatio.setDisable(true);
        }
    }

    @FXML
    public void registrarVivienda() {
        lblMensajes.setText("");
        lblMensajes.setTextFill(Color.RED);

        String tipo = cmbTipo.getValue();
        if (tipo == null) {
            lblMensajes.setText("Tipo: debe estar seleccionado.");
            return;
        }

        String codigo = txtCodigo.getText();
        if (codigo == null || codigo.trim().isEmpty()) {
            lblMensajes.setText("Código: no puede estar vacío.");
            return;
        }

        double superficie;
        try {
            superficie = Double.parseDouble(txtSuperficie.getText());
        } catch (NumberFormatException e) {
            lblMensajes.setText("Superficie: debe ser un número válido.");
            return;
        }

        int habitaciones;
        try {
            habitaciones = Integer.parseInt(txtHabitaciones.getText());
        } catch (NumberFormatException e) {
            lblMensajes.setText("Habitaciones: debe ser un número entero.");
            return;
        }

        try {
            if ("Departamento".equals(tipo)) {
                int piso;
                try {
                    piso = Integer.parseInt(txtPiso.getText());
                    if(piso <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    lblMensajes.setText("Piso: debe ser un número entero mayor a cero.");
                    return;
                }

                boolean gastoAlDia = chkGastoComun.isSelected();
                boolean estAsignado = chkEstacionamiento.isSelected();

                Departamento depto = new Departamento(codigo, superficie, habitaciones, piso, gastoAlDia);
                listaViviendas.add(depto);
                lblMensajes.setText(codigo + " (Departamento) registrada correctamente.");

            } else {
                boolean patio = chkPatio.isSelected();
                Casa casa = new Casa(codigo, superficie, habitaciones, patio);
                listaViviendas.add(casa);
                lblMensajes.setText(codigo + " (Casa) registrada correctamente.");
            }

            lblMensajes.setTextFill(Color.GREEN);
            actualizarArchivos();
            limpiarFormulario();

        } catch (Exception e) {
            lblMensajes.setText(e.getMessage());
        }
    }

    @FXML
    public void eliminarVivienda() {
        Vivienda seleccionada = tablaViviendas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            lblMensajes.setTextFill(Color.RED);
            lblMensajes.setText("Debe seleccionar una vivienda de la tabla para eliminarla.");
            return;
        }

        listaViviendas.remove(seleccionada);
        actualizarArchivos();
        lblMensajes.setTextFill(Color.GREEN);
        lblMensajes.setText("Vivienda eliminada correctamente.");
    }

    private void actualizarArchivos() {
        List<Departamento> deptos = new ArrayList<>();
        List<Casa> casas = new ArrayList<>();

        for (Vivienda v : listaViviendas) {
            if (v instanceof Departamento) {
                deptos.add((Departamento) v);
            } else if (v instanceof Casa) {
                casas.add((Casa) v);
            }
        }
        dao.guardarDepartamentos(deptos);
        dao.guardarCasas(casas);
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtSuperficie.clear();
        txtHabitaciones.clear();
        txtPiso.clear();
        chkGastoComun.setSelected(false);
        chkEstacionamiento.setSelected(false);
        chkPatio.setSelected(false);
        cmbTipo.getSelectionModel().clearSelection();
        cambiarTipoVivienda();
    }
}