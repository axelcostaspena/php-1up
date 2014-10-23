<?php

$foo = <<<EOT
fo<caret>o{$bar->baz(1.23, "qux$foo[bar]{$baz["qux"]}" . $foo)} bar
EOT;
