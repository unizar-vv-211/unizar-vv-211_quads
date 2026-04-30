package es.unizar.eina.T251_quads.test;

import io.cucumber.junit.CucumberOptions;
import org.junit.AfterClass;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

@CucumberOptions(features = { "features" })
public class RunCukesTest {

    @AfterClass
    public static void cleanDatabaseAfterAll() {
        // Limpiamos la base de datos SOLO una vez que han terminado todos los tests.
        // Esto permite que los escenarios dependan de datos creados en escenarios anteriores.
        Context context = ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.ReservaRepository reservaRepo = new es.unizar.eina.T251_quads.database.ReservaRepository(
                (android.app.Application) context);
        es.unizar.eina.T251_quads.database.QuadRepository quadRepo = new es.unizar.eina.T251_quads.database.QuadRepository(
                (android.app.Application) context);

        reservaRepo.deleteAll();
        quadRepo.deleteAll();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}