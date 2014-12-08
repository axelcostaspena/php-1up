<?php

$foo = <<<EOT
f<caret>oo{$bar->baz(1.23, "qux$foo[bar]{$baz["qux"]}" . $foo)} bar
EOT;
