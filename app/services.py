from __future__ import annotations

from calendar import monthrange
from datetime import date
from decimal import Decimal, ROUND_HALF_UP
from typing import Dict, List

from .models import (
    Consuntivo,
    ConsuntivoCreate,
    Documento,
    DocumentoInput,
    GeneraRateInput,
    PartecipanteRiparto,
    PianoSpesa,
    PianoSpesaCreate,
    Rata,
    Riparto,
    RipartoInput,
    StatoConsuntivo,
    VoceSpesa,
    VoceSpesaCreate,
)


class InMemoryStore:
    def __init__(self) -> None:
        self.piani: Dict[int, PianoSpesa] = {}
        self.consuntivi: Dict[int, Consuntivo] = {}
        self.riparti: Dict[int, Riparto] = {}
        self._piano_id = 1
        self._voce_id = 1
        self._riparto_id = 1
        self._rata_id = 1
        self._documento_id = 1
        self._consuntivo_id = 1

    def _next(self, counter: str) -> int:
        current = getattr(self, counter)
        setattr(self, counter, current + 1)
        return current

    def create_piano(self, payload: PianoSpesaCreate) -> PianoSpesa:
        piano = PianoSpesa(
            id=self._next("_piano_id"),
            nome=payload.nome,
            millesimi_totali=payload.millesimi_totali,
        )
        self.piani[piano.id] = piano
        return piano

    def add_voce(self, piano_id: int, payload: VoceSpesaCreate) -> VoceSpesa:
        piano = self._get_piano(piano_id)
        voce = VoceSpesa(
            id=self._next("_voce_id"),
            descrizione=payload.descrizione,
            importo_preventivo=payload.importo_preventivo,
        )
        piano.voci[voce.id] = voce
        return voce

    def _get_piano(self, piano_id: int) -> PianoSpesa:
        if piano_id not in self.piani:
            raise ValueError("Piano di spesa inesistente")
        return self.piani[piano_id]

    def _get_voce(self, piano: PianoSpesa, voce_id: int) -> VoceSpesa:
        if voce_id not in piano.voci:
            raise ValueError("Voce di spesa inesistente per il piano indicato")
        return piano.voci[voce_id]

    def calcola_riparto(self, piano_id: int, payload: RipartoInput) -> List[Riparto]:
        piano = self._get_piano(piano_id)
        voce = self._get_voce(piano, payload.voce_id)

        totale_millesimi = sum(partecipante.millesimi for partecipante in payload.partecipanti)
        if totale_millesimi > piano.millesimi_totali:
            raise ValueError("I millesimi superano il totale del piano")
        if totale_millesimi <= 0:
            raise ValueError("I millesimi devono essere maggiori di zero")

        importo_voce = Decimal(str(voce.importo_preventivo))
        riparti: List[Riparto] = []
        accumulated = Decimal("0")

        for index, partecipante in enumerate(payload.partecipanti):
            quota = (importo_voce * Decimal(partecipante.millesimi) / Decimal(piano.millesimi_totali)).quantize(
                Decimal("0.01"), rounding=ROUND_HALF_UP
            )

            if index == len(payload.partecipanti) - 1:
                # Adjust last quota to avoid rounding issues
                quota = importo_voce - accumulated
                quota = quota.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)

            riparto = Riparto(
                id=self._next("_riparto_id"),
                piano_id=piano.id,
                voce_id=voce.id,
                soggetto=partecipante.soggetto,
                millesimi=partecipante.millesimi,
                quota=float(quota),
            )
            riparti.append(riparto)
            accumulated += quota
            self.riparti[riparto.id] = riparto
        piano.riparti.extend(riparti)
        return riparti

    def genera_rate(self, piano_id: int, payload: GeneraRateInput) -> List[Rata]:
        piano = self._get_piano(piano_id)
        if payload.periodicita != "mensile":
            raise ValueError("Sono supportate solo rate mensili per questa versione")

        totale = Decimal(str(payload.totale))
        base_importo = (totale / Decimal(payload.numero_rate)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
        rate: List[Rata] = []
        accumulated = Decimal("0")

        for numero in range(payload.numero_rate):
            scadenza = self._aggiungi_mesi(payload.prima_scadenza, numero)
            importo = base_importo if numero < payload.numero_rate - 1 else (totale - accumulated)
            importo = importo.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
            rata = Rata(
                id=self._next("_rata_id"),
                piano_id=piano.id,
                importo=float(importo),
                scadenza=scadenza,
                descrizione=f"{payload.descrizione} {numero + 1}/{payload.numero_rate}",
            )
            accumulated += importo
            piano.rate.append(rata)
            rate.append(rata)
        return rate

    def allega_documento_preventivo(self, piano_id: int, payload: DocumentoInput) -> Documento:
        piano = self._get_piano(piano_id)
        documento = Documento(id=self._next("_documento_id"), filename=payload.filename, url=payload.url)
        piano.documenti_preventivo.append(documento)
        return documento

    def crea_consuntivo(self, payload: ConsuntivoCreate) -> Consuntivo:
        piano = self._get_piano(payload.piano_id)
        consuntivo = Consuntivo(
            id=self._next("_consuntivo_id"),
            piano_id=piano.id,
            totale_speso=payload.totale_speso,
        )
        self.consuntivi[consuntivo.id] = consuntivo
        piano.consuntivi.append(consuntivo.id)
        return consuntivo

    def chiudi_consuntivo(self, consuntivo_id: int, data: date) -> Consuntivo:
        if consuntivo_id not in self.consuntivi:
            raise ValueError("Consuntivo inesistente")
        consuntivo = self.consuntivi[consuntivo_id]
        if consuntivo.stato == StatoConsuntivo.chiuso:
            raise ValueError("Il consuntivo è già chiuso")
        consuntivo.stato = StatoConsuntivo.chiuso
        consuntivo.data_chiusura = data
        return consuntivo

    def allega_documento_consuntivo(self, consuntivo_id: int, payload: DocumentoInput) -> Documento:
        if consuntivo_id not in self.consuntivi:
            raise ValueError("Consuntivo inesistente")
        consuntivo = self.consuntivi[consuntivo_id]
        documento = Documento(id=self._next("_documento_id"), filename=payload.filename, url=payload.url)
        consuntivo.documenti.append(documento)
        return documento


    @staticmethod
    def _aggiungi_mesi(data_iniziale: date, mesi: int) -> date:
        anno = data_iniziale.year + (data_iniziale.month - 1 + mesi) // 12
        mese = (data_iniziale.month - 1 + mesi) % 12 + 1
        ultimo_giorno = monthrange(anno, mese)[1]
        giorno = min(data_iniziale.day, ultimo_giorno)
        return date(anno, mese, giorno)


store = InMemoryStore()
