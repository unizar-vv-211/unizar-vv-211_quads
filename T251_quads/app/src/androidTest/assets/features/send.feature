Feature: Envio de mensaje al cliente
  Como propietario
  Quiero enviar al cliente un mensaje con los datos de su reserva
  Para confirmar la reserva con sus fechas y quads asociados

  # CPA-SEND-01
  Scenario: Enviar mensaje de reserva correctamente
    Given Existe una reserva de envio para "Raul" con telefono "612345678" y precio total "50.0"
    When Envio por SMS la reserva de "Raul"
    Then Se debe generar un SMS con los datos de la reserva
