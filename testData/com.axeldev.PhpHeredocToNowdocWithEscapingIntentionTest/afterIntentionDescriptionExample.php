<?php

$foo = <<<'EOT'
This string has embedded
EOT
    . $variables . <<<'EOT'
and some escaped characters like A, A, and \
EOT;
