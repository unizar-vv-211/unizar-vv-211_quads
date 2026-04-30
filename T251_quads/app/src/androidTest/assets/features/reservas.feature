Feature: Gestión de Reservas
  Como usuario quiero gestionar reservas de quads
  asegurando que las reglas de precio y fechas se cumplen.

  Scenario: Crear una reserva valida
    Given Existe un quad con matricula "RES1111" y precio "50.0"
    And Estoy en la pantalla de crear reserva
    When Introduzco "Juan Perez" como cliente
    And Introduzco "600111222" como telefono
    And Introduzco "0" como numero de cascos
    And Selecciono la fecha "10-05-2026" como recogida
    And Selecciono la fecha "12-05-2026" como devolucion
    And Selecciono el quad "RES1111"
    And Confirmo la reserva
    Then Deberia ver la reserva de "Juan Perez" en la lista

  Scenario: Error si la fecha de devolucion es anterior a recogida
    And Estoy en la pantalla de crear reserva
    When Selecciono la fecha "15-05-2026" como recogida
    And Selecciono la fecha "10-05-2026" como devolucion
    And Confirmo la reserva
    Then Deberia ver un error de fecha invalida

  Scenario: Comprobar mantenimiento de precio (Precio Congelado)
    Given Existe un quad con matricula "RES1111" y precio "50.0"
    And Existe una reserva para "Ana Lopez" vinculada al quad "RES1111"
    When Cambio el precio del quad "RES1111" a "90.0" euros por dia
    And Voy a la lista de reservas
    And Visualizo la reserva de "Ana Lopez"
    Then El precio de la reserva deberia ser "100.0"