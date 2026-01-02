from datetime import date

import pytest

from app.models import ConsuntivoCreate, GeneraRateInput, PartecipanteRiparto, PianoSpesaCreate, RipartoInput, VoceSpesaCreate
from app.services import InMemoryStore


def test_riparto_millesimi_totals_match_importo():
    store = InMemoryStore()
    piano = store.create_piano(PianoSpesaCreate(nome="Bilancio 2026", millesimi_totali=1000))
    voce = store.add_voce(piano.id, VoceSpesaCreate(descrizione="Pulizie", importo_preventivo=1000))
    riparti = store.calcola_riparto(
        piano.id,
        RipartoInput(
            voce_id=voce.id,
            partecipanti=[
                PartecipanteRiparto(soggetto="Scala A", millesimi=600),
                PartecipanteRiparto(soggetto="Scala B", millesimi=400),
            ],
        ),
    )

    totale_ripartito = sum(r.quota for r in riparti)
    assert pytest.approx(totale_ripartito, rel=1e-3) == voce.importo_preventivo
    assert {r.soggetto for r in riparti} == {"Scala A", "Scala B"}


def test_schedulazione_generazione_rate_mensili():
    store = InMemoryStore()
    piano = store.create_piano(PianoSpesaCreate(nome="Bilancio 2026", millesimi_totali=1000))
    payload = GeneraRateInput(
        totale=2400,
        numero_rate=4,
        prima_scadenza=date(2026, 1, 31),
        periodicita="mensile",
    )
    rate = store.genera_rate(piano.id, payload)

    assert len(rate) == 4
    assert all(r.importo == 600 for r in rate)
    assert [r.scadenza for r in rate] == [
        date(2026, 1, 31),
        date(2026, 2, 28),
        date(2026, 3, 31),
        date(2026, 4, 30),
    ]


def test_chiusura_consuntivo_blocca_doppia_chiusura():
    store = InMemoryStore()
    piano = store.create_piano(PianoSpesaCreate(nome="Consuntivo"))
    consuntivo = store.crea_consuntivo(ConsuntivoCreate(piano_id=piano.id, totale_speso=3200))

    chiuso = store.chiudi_consuntivo(consuntivo.id, date(2026, 12, 31))
    assert chiuso.stato.value == "chiuso"
    assert chiuso.data_chiusura == date(2026, 12, 31)

    with pytest.raises(ValueError):
        store.chiudi_consuntivo(consuntivo.id, date(2027, 1, 1))
