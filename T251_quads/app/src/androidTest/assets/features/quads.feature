Feature: Gestion del inventario de quads
  Como empleado de la tienda de alquiler
  Quiero mantener actualizado el catalogo de quads
  Para poder asignarlos posteriormente a las reservas de los clientes

  # CPA-Q-01, CPA-Q-02
  Scenario Outline: Crear quad valido
    Given Estoy en la pantalla principal de Quads
    When Hago clic en crear un quad
    And Introduzco "<matricula>" como matricula
    And Selecciono el tipo "<tipo>"
    And Introduzco "<precio>" como precio
    And Introduzco "<descripcion>" como descripcion
    And Confirmo la creacion
    Then Deberia ver "<matricula>" en la lista

    Examples:
      | matricula | tipo      | precio | descripcion               |
      | AAA1111   | Monoplaza | 10.0   | Quad valido monoplaza     |
      | AAA1112   | Biplaza   | 10.0   | Quad valido biplaza       |

  # CPA-Q-03, CPA-Q-04, CPA-Q-05, CPA-Q-06, CPA-Q-07
  Scenario Outline: Rechazar quad con datos invalidos
    Given Estoy en la pantalla principal de Quads
    When Hago clic en crear un quad
    And Introduzco "<matricula>" como matricula
    And Selecciono el tipo "<tipo>"
    And Introduzco "<precio>" como precio
    And Introduzco "<descripcion>" como descripcion
    And Confirmo la creacion
    Then El sistema debe mantenerme en la pantalla de creacion de Quads

    Examples:
      | matricula | tipo      | precio | descripcion             |
      | 1111AAA   | Monoplaza | 10.0   | Formato invalido        |
      |           | Monoplaza | 10.0   | Matricula vacia         |
      | AAA1113   | Monoplaza | -10.0  | Precio negativo         |
      | AAA1114   | Monoplaza |        | Precio vacio            |
      | AAA1115   | Monoplaza | 10.0   |                         |
