from __future__ import annotations

from datetime import date

from .models import (
    Consuntivo,
    ConsuntivoCreate,
    Documento,
    DocumentoInput,
    GeneraRateInput,
    PianoSpesa,
    PianoSpesaCreate,
    Rata,
    Riparto,
    RipartoInput,
    VoceSpesa,
    VoceSpesaCreate,
)
from .services import store


def crea_piano(payload: PianoSpesaCreate) -> PianoSpesa:
    return store.create_piano(payload)


def aggiungi_voce(piano_id: int, payload: VoceSpesaCreate) -> VoceSpesa:
    return store.add_voce(piano_id, payload)


def calcola_riparto(piano_id: int, payload: RipartoInput) -> list[Riparto]:
    return store.calcola_riparto(piano_id, payload)


def genera_rate(piano_id: int, payload: GeneraRateInput) -> list[Rata]:
    return store.genera_rate(piano_id, payload)


def allega_documento_preventivo(piano_id: int, payload: DocumentoInput) -> Documento:
    return store.allega_documento_preventivo(piano_id, payload)


def crea_consuntivo(payload: ConsuntivoCreate) -> Consuntivo:
    return store.crea_consuntivo(payload)


def chiudi_consuntivo(consuntivo_id: int, data_chiusura: date) -> Consuntivo:
    return store.chiudi_consuntivo(consuntivo_id, data_chiusura)


def allega_documento_consuntivo(consuntivo_id: int, payload: DocumentoInput) -> Documento:
    return store.allega_documento_consuntivo(consuntivo_id, payload)
