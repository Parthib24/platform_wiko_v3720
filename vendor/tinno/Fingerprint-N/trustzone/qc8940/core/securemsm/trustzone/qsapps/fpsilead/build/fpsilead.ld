PHDRS {
  CODE_RO PT_LOAD;
  DATA PT_LOAD;
  GOT PT_LOAD;
  REL_H PT_LOAD;
  REL_D PT_LOAD;
}

SECTIONS {
  .text : {
    KEEP(*\tzapp_entry.o (TZAPPENTRYCODE))
    *(.text .text.* kthread_shared)
    KEEP(*(tzapp_syscall_asm))
    *(.rodata .rodata.*)
   } : CODE_RO

 .data ALIGN(4096): {
    * (.data TZAPPENTRYSTACK .bss COMMON)
  } : DATA

  .rel.text : {
    *(.rel.text)
  }
  .rel.rodata : {
    *(.rel.rodata)
  }
  .data.rel.ro.local : {
    *(.data.rel.ro.local)
  }
  .data.rel.ro : {
    *(.data.rel.ro)
  }
  .rel.data : {
    *(.rel.data)
  }
  .rel.plt : {
    *(.rel.plt)
  }
  .data.rel : {
    *(.data.rel)
  }
  .data.rel.local : {
    *(.data.rel.local)
  }

  TZ_APP_STACK : {
    Image$$TZ_APP_STACK$$ZI$$Base = .;
    . = . + 0x8000;
    Image$$TZ_APP_STACK$$ZI$$Limit = .;
  }
  TZ_APP_HEAP : {
    Image$$TZ_APP_HEAP$$ZI$$Base = .;
    . = . + 0x380000;
    Image$$TZ_APP_HEAP$$ZI$$Limit = .;
  }

  .got : ALIGN(4096) { *(.got.plt) *(.igot.plt) *(.got) *(.igot) } : GOT

  .dynamic : ALIGN(4096) { *(.dynamic) }: REL_H

  .rel.dyn : ALIGN(4096) {*(.rel.dyn)} : REL_D

  .gnu.attributes 0 : { KEEP (*(.gnu.attributes)) }
  .note.gnu.arm.ident 0 : { KEEP (*(.note.gnu.arm.ident)) }
  /DISCARD/ : { *(.hash) *(.dynsym) *(.dynstr) *(.ARM.exidx*) *(.note.GNU-stack) *(.gnu_debuglink) *(.gnu.lto_*) *(.init) *(.fini) }
}
