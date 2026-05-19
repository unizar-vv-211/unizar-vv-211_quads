Feature: Consulta del inventario de quads
  Como propietario
  Quiero consultar el inventario de quads
  Para conocer que vehiculos estan registrados y ordenarlos por distintos criterios

  # CPA-CQ-01
  Scenario: Consultar inventario con quads registrados
    Given Existen quads registrados en el inventario
    And Estoy en la pantalla principal de Quads
    Then El sistema muestra el inventario de quads

  # CPA-CQ-03
  Scenario Outline: Ordenar inventario de quads
    Given Existen quads registrados en el inventario
    And Estoy en la pantalla principal de Quads
    When Solicito ordenar los quads por "<criterio>"
    Then El listado de quads se actualiza segun "<criterio>"

    Examples:
      | criterio  |
      | Matricula |
      | Tipo      |
      | Precio    |
