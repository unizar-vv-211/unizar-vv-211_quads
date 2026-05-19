Feature: Creacion de reservas
  Como propietario
  Quiero registrar reservas validas
  Para gestionar la disponibilidad de quads

  # CPA-R-01
  Scenario: Crear reserva valida con fecha de devolucion posterior
    Given Existe un quad con matricula "AAA1111" y precio "10.0"
    And Estoy en la pantalla principal de Reservas
    When Hago clic en crear una reserva
    And Introduzco "Raul" como cliente
    And Introduzco "123456789" como telefono
    And Introduzco "10-05-2026" como fecha de recogida
    And Introduzco "11-05-2026" como fecha de devolucion
    And Introduzco "1" como numero de cascos
    And Selecciono los quads "AAA1111"
    And Confirmo la creacion de la reserva
    Then Deberia ver "Raul" en la lista de reservas
