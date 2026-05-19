Feature: Visualizacion de reservas por estado temporal
  Como propietario
  Quiero filtrar las reservas por estado temporal
  Para consultar rapidamente reservas previstas, vigentes y caducadas

  Background:
    Given Existen reservas previstas, vigentes y caducadas

  # CPA-E-01
  Scenario: Mostrar todas las reservas por defecto
    Given Estoy en la pantalla principal de Reservas
    Then Deberia ver "Prevista" en la lista de reservas
    And Deberia ver "Vigente" en la lista de reservas
    And Deberia ver "Caducada" en la lista de reservas

  # CPA-E-02
  Scenario: Filtrar reservas previstas
    Given Estoy en la pantalla principal de Reservas
    When Selecciono el filtro de reservas "Futuras"
    Then Deberia ver "Prevista" en la lista de reservas
    And No deberia ver "Vigente" en la lista de reservas
    And No deberia ver "Caducada" en la lista de reservas

  # CPA-E-03
  Scenario: Filtrar reservas vigentes
    Given Estoy en la pantalla principal de Reservas
    When Selecciono el filtro de reservas "Activas"
    Then Deberia ver "Vigente" en la lista de reservas
    And No deberia ver "Prevista" en la lista de reservas
    And No deberia ver "Caducada" en la lista de reservas

  # CPA-E-04
  Scenario: Filtrar reservas caducadas
    Given Estoy en la pantalla principal de Reservas
    When Selecciono el filtro de reservas "Pasadas"
    Then Deberia ver "Caducada" en la lista de reservas
    And No deberia ver "Prevista" en la lista de reservas
    And No deberia ver "Vigente" en la lista de reservas
