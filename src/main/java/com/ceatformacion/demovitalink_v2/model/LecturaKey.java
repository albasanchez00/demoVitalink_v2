package com.ceatformacion.demovitalink_v2.model;

import java.io.Serializable;
import java.util.Objects;

public class LecturaKey implements Serializable {
    private Integer mensaje;
    private Integer usuario;
    // equals/hashCode
    @Override public boolean equals(Object o){ if(this==o) return true; if(!(o instanceof LecturaKey k)) return false; return Objects.equals(mensaje,k.mensaje)&&Objects.equals(usuario,k.usuario);}
    @Override public int hashCode(){ return Objects.hash(mensaje,usuario); }
}