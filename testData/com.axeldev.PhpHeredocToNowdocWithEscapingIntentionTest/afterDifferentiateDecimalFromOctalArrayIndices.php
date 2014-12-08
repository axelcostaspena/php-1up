<?php

$foo = $foo[0] . <<<'EOT'
bar
EOT
    . $foo['00'] . <<<'EOT'
baz
EOT
    . $bar[1] . <<<'EOT'
qux
EOT
    . $baz['01'] . <<<'EOT'
foo
EOT
    . $qux[10];
