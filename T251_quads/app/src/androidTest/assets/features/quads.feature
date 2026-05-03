Feature: Gestion del Inventario de Quads (RF-1)
  Como empleado de la tienda de alquiler
  Quiero mantener actualizado el catalogo de quads
  Para poder asignarlos posteriormente a las reservas de los clientes

  # SCENARIO TESTING
  Scenario Outline: Incorporar un nuevo quad a la flota de alquiler
    Given Estoy en la pantalla principal de Quads
    When Hago clic en crear un quad
    And Introduzco "<matricula>" como matricula
    And Introduzco "<precio>" como precio
    And Introduzco "<descripcion>" como descripcion
    And Confirmo la creacion
    Then Deberia ver "<matricula>" en la lista

    Examples:
      | matricula | precio | descripcion                     |
      | ABC1234   | 50.5   | Quad estandar para reservas     |
      | XYZ5678   | 80.0   | Quad premium para rutas largas  |

  # SCENARIO TESTING (Prevencion de Errores Comerciales)
  Scenario Outline: Rechazo de quads con informacion comercial ilogica o incompleta
    Given Estoy en la pantalla principal de Quads
    When Hago clic en crear un quad
    And Introduzco "<matricula>" como matricula
    And Introduzco "<precio>" como precio
    And Introduzco "<descripcion>" como descripcion
    And Confirmo la creacion
    Then El sistema debe mantenerme en la pantalla de creacion de Quads

    Examples:
      | caso_de_negocio                  | matricula | precio | descripcion          |
      | Vehiculo sin placa identificable |           | 50.0   | Faltan datos         |
      | Tarifa de alquiler no rentable   | LMN4321   | -15.5  | Precio imposible     |
      | Matricula invalida para trafico  | 456ASDA   | 40.0   | Error administrativo |