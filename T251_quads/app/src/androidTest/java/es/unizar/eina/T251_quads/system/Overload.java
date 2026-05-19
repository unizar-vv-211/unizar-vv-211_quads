package es.unizar.eina.T251_quads.system;

import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.database.Reserva;
import es.unizar.eina.T251_quads.database.ReservaRepository;

@RunWith(AndroidJUnit4.class)
public class Overload {

    @Test
    public void concurrenciaExtremaDBTest() {
        try {
            Application app = ApplicationProvider.getApplicationContext();
            QuadRepository quadRepo = new QuadRepository(app);
            ReservaRepository reservaRepo = new ReservaRepository(app);

            // Creamos un Quad objetivo
            quadRepo.insert(new Quad("OVR1111", "Monoplaza", Float.MAX_VALUE, "Quad para overload"));

            // 50 operaciones haciéndose A LA VEZ en la base de datos (sin interfaz gráfica)
            int numThreads = 50;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int i = 0; i < numThreads; i++) {
                final int threadId = i;
                executor.execute(() -> {
                    try {
                        // Inserciones simultáneas de reservas para el MISMO Quad en las MISMAS fechas
                        Reserva r = new Reserva("Cliente " + threadId, "999999999", "0001-01-01", "9999-12-31", Integer.MAX_VALUE, Double.MAX_VALUE);
                        reservaRepo.insert(r, Collections.singletonList("OVR1111"));

                        // Y a la vez actualizaciones simultáneas del Quad original (fuerza condiciones
                        // de carrera en Room/SQLite)
                        Quad q = new Quad("OVR1111", "Biplaza", Float.MAX_VALUE, "Updated");
                        quadRepo.update(q);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Esperamos que terminen los hilos
            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            org.junit.Assert.fail("Fallo.");
        } catch (Throwable t) {
            System.out.println(
                    ">>> ÉXITO: La aplicación ha colapsado (" + t.getClass().getSimpleName() + "): " + t.getMessage());
        }
    }

    @org.junit.After
    public void limpiarBaseDeDatos() {
        Application app = ApplicationProvider.getApplicationContext();
        QuadRepository quadRepo = new QuadRepository(app);
        ReservaRepository reservaRepo = new ReservaRepository(app);

        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            reservaRepo.deleteAll();
            quadRepo.deleteAll();
            latch.countDown();
        }).start();

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
