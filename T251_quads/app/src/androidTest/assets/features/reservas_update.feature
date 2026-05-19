Feature: Modificacion de reservas
  Como propietario
  Quiero modificar reservas existentes
  Para corregir sus datos manteniendo las reglas de validacion y disponibilidad

  # CPA-U-01
  Scenario: Modificar cliente de una reserva existente
    Given Existe un quad con matricula "AAA1111" y precio "10.0"
    And Existe una reserva registrada para "Raul" con el quad "AAA1111" del "10-05-2026" al "11-05-2026" y precio "10.0"
    And Estoy en la pantalla principal de Reservas
    When Selecciono la reserva de "Raul"
    And Elijo modificar la reserva
    And Introduzco "Samuel" como cliente
    And Confirmo la creacion de la reserva
    Then Deberia ver "Samuel" en la lista de reservas

  # CPA-U-02
  Scenario: Rechazar actualizacion con fecha de devolucion anterior
    Given Existe un quad con matricula "AAA1111" y precio "10.0"
    And Existe una reserva registrada para "Raul" con el quad "AAA1111" del "10-05-2026" al "11-05-2026" y precio "10.0"
    And Estoy en la pantalla principal de Reservas
    When Selecciono la reserva de "Raul"
    And Elijo modificar la reserva
    And Introduzco "09-05-2026" como fecha de devolucion
    And Confirmo la creacion de la reserva
    Then El sistema debe mantenerme en la pantalla de creacion de Reservas

  # CPA-U-03
  Scenario: Rechazar actualizacion que provoca solapamiento
    Given Existe un quad con matricula "AAA1111" y precio "10.0"
    And Existe una reserva previa para "Ocupada" con el quad "AAA1111" del "10-05-2026" al "15-05-2026"
    And Existe una reserva registrada para "Raul" con el quad "AAA1111" del "16-05-2026" al "18-05-2026" y precio "10.0"
    And Estoy en la pantalla principal de Reservas
    When Selecciono la reserva de "Raul"
    And Elijo modificar la reserva
    And Introduzco "13-05-2026" como fecha de recogida
    And Introduzco "18-05-2026" como fecha de devolucion
    And Confirmo la creacion de la reserva
    Then El sistema debe mantenerme en la pantalla de creacion de Reservas
