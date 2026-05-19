Feature: Eliminacion de reservas
  Como propietario
  Quiero eliminar reservas
  Para retirar reservas que ya no deben mantenerse en el sistema

  # CPA-DR-01
  Scenario: Eliminar reserva existente
    Given Existe un quad con matricula "AAA1111" y precio "10.0"
    And Existe una reserva registrada para "Raul" con el quad "AAA1111" del "10-05-2026" al "11-05-2026" y precio "10.0"
    And Estoy en la pantalla principal de Reservas
    When Selecciono la reserva de "Raul"
    And Elijo eliminar la reserva
    Then No deberia ver "Raul" en la lista de reservas
