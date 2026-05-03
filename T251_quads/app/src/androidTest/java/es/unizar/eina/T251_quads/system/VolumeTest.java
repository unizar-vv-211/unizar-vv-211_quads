// Package de pruebas de sistema de volumen de reservas
package es.unizar.eina.T251_quads.system;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import es.unizar.eina.T251_quads.database.QuadRoomDatabase;
import es.unizar.eina.T251_quads.database.Reserva;
import es.unizar.eina.T251_quads.database.ReservaDao;

import es.unizar.eina.T251_quads.ui.ReservaListActivity;
import es.unizar.eina.T251_quads.R;

@RunWith(AndroidJUnit4.class)
public class VolumeTest {

    private QuadRoomDatabase db;
    private ReservaDao reservaDao;

    @Before
    public void setUp() {
        // Obtenemos el contexto de la aplicación de prueba
        Context context = ApplicationProvider.getApplicationContext();
        // Inicializamos la BD y el DAO
        db = QuadRoomDatabase.getDatabase(context);
        reservaDao = db.reservaDao();

        // Partimos de un estado limpio
        reservaDao.deleteAll();
    }

    @After
    public void tearDown() {
        // Limpiamos al terminar
        reservaDao.deleteAll();
    }

    @Test
    public void testEficiencia_Soporta20000Reservas() {
        // 1. FASE DE PREPARACIÓN (Seeder de Volumen)
        List<Reserva> volumenReservas = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            // Creamos instancias directas.
            volumenReservas.add(new Reserva(
                    "Cliente " + i,
                    "600000000",
                    "2026-06-01",
                    "2026-06-10",
                    2, // Cascos
                    150.0 // Precio congelado
            ));
        }

        // Inyectamos en BD de golpe.
        db.runInTransaction(() -> {
            for (Reserva r : volumenReservas) {
                reservaDao.insert(r);
            }
        });

        // 2. FASE DE EJECUCIÓN
        try (ActivityScenario<ReservaListActivity> scenario = ActivityScenario.launch(ReservaListActivity.class)) {

            // 3. FASE DE ASERCIÓN
            onView(withId(R.id.recyclerview))
                    .check(matches(isDisplayed()));
        }
    }
}